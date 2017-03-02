package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.overlay.LookupTable
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.network.Address
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.kompics.sl._
import se.sics.kompics.Start

object BootstrapMaster {

  case class Init(self: Address, bootThreshold: Int, keepAlivePeriod: Long)
    extends se.sics.kompics.Init[BootstrapMaster]

}

class BootstrapMaster(init: BootstrapMaster.Init) extends ComponentDefinition {

  class BootstrapTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

  sealed trait State
  case object Collecting extends State
  case object Seeding extends State
  case object Done extends State

  val log = LoggerFactory.getLogger(classOf[BootstrapMaster])

  val boot = provides[Bootstrapping]
  val pl = requires[PerfectLink]
  val timer = requires[Timer]

  val self = init.self
  val bootThreshold = init.bootThreshold
  val period = 2 * init.keepAlivePeriod

  var state: State = Collecting
  var timeoutId: UUID = _
  var active: Set[Address] = Set()
  var ready: Set[Address] = Set()
  var initialAssignment: LookupTable = _

  ctrl uponEvent {
    case _: Start => handle {
      log.debug(s"Initiated bootstrapping procedure at $self...")
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
            log.debug(s"Nodes at $active are waiting for initial assignment...")
            state = Seeding
            trigger(GetInitialAssignments(active) -> boot)
          }
        case Seeding =>
          if (ready.size >= bootThreshold) {
            log.debug(s"Nodes at $ready are ready to boot, process complete.")
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
      log.trace(s"Slave at $src is waiting for initial assignment.")
      active += src
    }
    case PL_Deliver(src, Ready) => handle {
      log.trace(s"Slave at $src is ready to boot.")
      ready += src
    }
  }

}

