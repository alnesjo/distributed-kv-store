package se.kth.id2203.failure

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}

object HeartbeatFailureDetector {

  case class Init(self: Address, topology: Set[Address], period: Long) extends se.sics.kompics.Init[HeartbeatFailureDetector]

}

class HeartbeatFailureDetector(init: HeartbeatFailureDetector.Init) extends ComponentDefinition {

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)

  def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(delay)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

  val log = LoggerFactory.getLogger(classOf[HeartbeatFailureDetector])

  val epfd = provides[EventuallyPerfectFailureDetector]
  val pl = requires[PerfectLink]
  val timer = requires[Timer]

  val self = init.self
  var period = init.period
  val topology = init.topology
  var alive = init.topology
  var suspected = Set.empty[Address]
  var seqnum = 0

  ctrl uponEvent {
    case _: Start => handle {
      startTimer(period)
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      if (alive.intersect(suspected).nonEmpty) {
        suspected --= alive
      }
      seqnum += 1
      for (p <- topology) {
        if (!alive.contains(p) && !suspected.contains(p)) {
          // We don't know if process p is alive, and process p is not suspected.
          // Therefore we should start suspecting process p.
          suspected += p
          log.trace(s"Suspected failure on $p.")
          trigger(EP_Suspect(p) -> epfd)
        } else if (alive.contains(p) && suspected.contains(p)) {
          suspected -= p
          log.trace(s"Restored trust in $p.")
          trigger(EP_Restore(p) -> epfd)
        }
        trigger(PL_Send(p, HeartbeatRequest(seqnum)) -> pl)
      }
      alive = Set[Address]()
      startTimer(period)
    }
  }

  pl uponEvent {
    case PL_Deliver(src, HeartbeatRequest(seq)) => handle {
      trigger(PL_Send(src, HeartbeatReply(seq)) -> pl)
    }
    case PL_Deliver(src, HeartbeatReply(_)) => handle {
      alive += src
    }
  }

}
