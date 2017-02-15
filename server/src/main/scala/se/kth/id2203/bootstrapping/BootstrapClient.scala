package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.{BestEffortBroadcast, PerfectLink, Send}
import se.sics.kompics.{ClassMatchedHandler, Start}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timer, CancelPeriodicTimeout}

class BootstrapClient extends ComponentDefinition {

  sealed trait State
  case object Waiting extends State
  case object Started extends State

  override def tearDown() = trigger(new CancelPeriodicTimeout(timeoutId) -> timer)

  val log = LoggerFactory.getLogger(classOf[BootstrapClient])

  val boot = provides(Bootstrapping)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = cfg.getValue[Address]("id2203.project.address")
  val server = cfg.getValue[Address]("id2203.project.bootstrap-address")

  var state: State = Waiting
  var timeoutId: UUID = _

  ctrl uponEvent {
    case _: Start => handle {
      log.debug("Starting bootstrap client on {}", self)
      val period = 2 * (config getValue("id2203.project.keepAlivePeriod", classOf[Long]))
      val spt = new SchedulePeriodicTimeout(period, period)
      spt.setTimeoutEvent(new BootstrapTimeout(spt))
      trigger(spt -> timer)
      timeoutId = spt.getTimeoutEvent.getTimeoutId
    }
  }

  timer uponEvent {
    case _: BootstrapTimeout => handle {
      state match {
        case Waiting => {
          trigger(Send(server, Active) -> pl)
        }
        case Started => {
          trigger(Send(server, Ready) -> pl)
          suicide()
        }
      }
    }
  }

  pl uponEvent {
    case Boot(assignment: NodeAssignment) => handle {
      if (state == Waiting) {
        log.info("{} Booting up.", self)
        trigger(Booted(assignment) -> boot)
        trigger(new CancelPeriodicTimeout(timeoutId) -> timer)
        trigger(Send(server, Ready) -> pl)
        state = Started
      }
    }
  }

}
