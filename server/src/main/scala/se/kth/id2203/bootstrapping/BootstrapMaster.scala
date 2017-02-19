package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.network.Address
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}
import se.sics.kompics.sl._
import se.sics.kompics.Start

object BootstrapMaster {

  case class Init(self: Address,
                  bootThreshold: Int,
                  keepAlivePeriod: Long)
    extends se.sics.kompics.Init[BootstrapMaster]

  sealed trait State
  case object Collecting extends State
  case object Seeding extends State
  case object Done extends State

}

class BootstrapMaster(init: BootstrapMaster.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[BootstrapMaster])

  val boot = provides(Bootstrapping)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = init.self
  val bootThreshold = init.bootThreshold
  val period = 2 * init.keepAlivePeriod

  var state: BootstrapMaster.State = BootstrapMaster.Collecting
  var timeoutId: UUID = _
  var active = Set[Address]()
  var ready = Set[Address]()
  var initialAssignment: NodeAssignment = _

  ctrl uponEvent {
    case _: Start => handle {
      println(s"Boostrapping master $self initiated bootstrapping procedure...")
      println("Collecting...")
      val spt = new SchedulePeriodicTimeout(period, period)
      spt.setTimeoutEvent(new BootstrapTimeout(spt))
      trigger(spt, timer)
      timeoutId = spt.getTimeoutEvent.getTimeoutId
      active += self
    }
  }

  timer uponEvent {
    case _: BootstrapTimeout => handle {
      state match {
        case BootstrapMaster.Collecting =>
          if (active.size >= bootThreshold) {
            println("Seeding...")
            state = BootstrapMaster.Seeding
            trigger(GetInitialAssignments(active) -> boot)
          }
        case BootstrapMaster.Seeding =>
          if (ready.size >= bootThreshold) {
            println("Done.")
            trigger(Booted(initialAssignment) -> boot)
            state = BootstrapMaster.Done
          }
        case BootstrapMaster.Done =>
          trigger(new CancelPeriodicTimeout(timeoutId) -> timer)
          suicide()
      }
    }
  }

  boot uponEvent {
    case InitialAssignments(assignment) => handle {
      println("Seeding...")
      initialAssignment = assignment
      for (n <- active) trigger(PL_Send(n, Boot(initialAssignment)) -> pl)
      ready += self
    }
  }

  pl uponEvent {
    case PL_Deliver(src, Active) => handle {
      println("Collecting...")
      active += src
    }
    case PL_Deliver(src, Ready) => handle {
      println("Seeding...")
      ready += src
    }
  }

}

