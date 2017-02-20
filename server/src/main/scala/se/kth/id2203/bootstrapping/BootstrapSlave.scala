package se.kth.id2203.bootstrapping

import java.util.UUID

import org.slf4j.LoggerFactory
import se.kth.id2203.link.NetworkMessage
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.Start
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timer}

object BootstrapSlave {

  case class Init(self: Address, master: Address, keepAlivePeriod: Long) extends se.sics.kompics.Init[BootstrapSlave]
}

class BootstrapSlave(init: BootstrapSlave.Init) extends ComponentDefinition {

  sealed trait State
  case object Waiting extends State
  case object Started extends State

  override def tearDown() = trigger(new CancelPeriodicTimeout(timeoutId) -> timer)

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
      println(s"Boostrapping slave $self initiated bootstrapping procedure...")
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
          println("Waiting for master...")
          trigger(PL_Send(master, Active) -> pl)
        }
        case Started => {
          println("Ready to boot.")
          trigger(PL_Send(master, Ready) -> pl)
          suicide()
        }
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(_, Boot(assignment: NodeAssignment)) => handle {
      if (state == Waiting) {
        println(s"Received  $self...")
        trigger(Booted(assignment) -> boot)
        trigger(new CancelPeriodicTimeout(timeoutId) -> timer)
        trigger(PL_Send(master, Ready) -> pl)
        state = Started
      }
    }
  }

}
