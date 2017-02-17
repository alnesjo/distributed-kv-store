package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.network.Address
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}
import se.sics.kompics.sl._
import se.sics.kompics.Start

class BootstrapMaster extends ComponentDefinition {

  sealed trait State
  case object Collecting extends State
  case object Seeding extends State
  case object Done extends State

  val log = LoggerFactory.getLogger(classOf[BootstrapMaster])

  val boot = provides(Bootstrapping)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = cfg.getValue[Address]("id2203.project.address")
  val bootThreshold = cfg.getValue[Int]("id2203.project.bootThreshold")

  var state: State = Collecting
  var timeoutId: UUID = _
  var active = Set[Address]()
  var ready = Set[Address]()
  var initialAssignment: NodeAssignment = _

  ctrl uponEvent {
    case _: Start => handle {
      val period = 2 * (config getValue("id2203.project.keepAlivePeriod", classOf[Long]))
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
        case Collecting =>
          if (active.size >= bootThreshold) {
            state = Seeding
            trigger(GetInitialAssignments(active) -> boot)
          }
        case Seeding =>
          if (ready.size >= bootThreshold) {
            trigger(Booted(initialAssignment) -> boot)
            state = Done
          }
        case Done =>
          trigger(new CancelPeriodicTimeout(timeoutId) -> timer)
          suicide()
      }
    }
  }

  boot uponEvent {
    case InitialAssignments(assignment) => handle {
      initialAssignment = assignment
      for (n <- active) trigger(PL_Send(n, Boot(initialAssignment)) -> pl)
      ready += self
    }
  }

  pl uponEvent {
    case PL_Deliver(src, Active) => handle {
      active += src
    }
    case PL_Deliver(src, Ready) => handle {
      ready += src
    }
  }

}

