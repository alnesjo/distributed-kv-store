package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.Start
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}

object BootstrapSlave {

  case class Init(self: Address, master: Address, keepAlivePeriod: Long) extends se.sics.kompics.Init[BootstrapSlave]

}

class BootstrapSlave(init: BootstrapSlave.Init) extends ComponentDefinition {

  class BootstrapTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

  sealed trait State
  case object Waiting extends State
  case object Started extends State

  override def tearDown() = trigger(new CancelPeriodicTimeout(timeoutId) -> timer)

  val log = LoggerFactory.getLogger(classOf[BootstrapSlave])

  val boot = provides(Bootstrapping)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = init.self
  val master = init.master
  val period = 2 * init.keepAlivePeriod

  var state: State = Waiting
  var timeoutId: UUID = _

  ctrl uponEvent {
    case _: Start => handle {
      log.debug(s"Boostrapping slave at $self, initiated bootstrapping procedure. Waiting for master at $master ...")
      val spt = new SchedulePeriodicTimeout(period, period)
      spt.setTimeoutEvent(new BootstrapTimeout(spt))
      trigger(spt -> timer)
      timeoutId = spt.getTimeoutEvent.getTimeoutId
      trigger(PL_Send(master, Active) -> pl)
    }
  }

  timer uponEvent {
    case _: BootstrapTimeout => handle {
      state match {
        case Started =>
          log.debug("Ready to boot.")
          trigger(PL_Send(master, Ready) -> pl)
          suicide()
        case _ =>
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(`master`, Boot(assignment: NodeAssignment)) => handle {
      if (state == Waiting) {
        log.debug(s"Received initial assignment from master at $master.")
        trigger(Booted(assignment) -> boot)
        trigger(new CancelPeriodicTimeout(timeoutId) -> timer)
        trigger(PL_Send(master, Ready) -> pl)
        state = Started
      }
    }
  }

}
