package se.kth.id2203.failure

import se.kth.id2203._
import se.sics.kompics.{KompicsEvent, Start}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}

class HeartbeatFailureDetector(epfdInit: Init[HeartbeatFailureDetector]) extends ComponentDefinition {

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)

  case class HeartbeatReply(seq: Int) extends KompicsEvent
  case class HeartbeatRequest(seq: Int) extends KompicsEvent

  val epfd = provides(EventuallyPerfectFailureDetector)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = epfdInit match {case Init(s: Address) => s}
  val topology = cfg.getValue[List[Address]]("epfd.simulation.topology")
  val delta = cfg.getValue[Long]("epfd.simulation.delay")

  var period = cfg.getValue[Long]("epfd.simulation.delay")
  var alive = Set(cfg.getValue[List[Address]]("epfd.simulation.topology"): _*)
  var suspected = Set[Address]()
  var seqnum = 0

  def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

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
          trigger(EP_Suspect(p) -> epfd)
        } else if (alive.contains(p) && suspected.contains(p)) {
          suspected -= p
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
    case PL_Deliver(src, HeartbeatReply(seq)) => handle {
      alive += src
    }
  }

}
