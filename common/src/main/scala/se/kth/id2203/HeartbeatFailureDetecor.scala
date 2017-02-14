package se.kth.id2203

import se.sics.kompics.Start
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timer}

class HeartbeatFailureDetecor extends ComponentDefinition {

  val epfd = provides(EventuallyPerfectFailureDetector)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  //configuration parameters
  val self = epfdInit match {case Init(s: Address) => s}
  val topology = cfg.getValue[List[Address]]("epfd.simulation.topology")
  val delta = cfg.getValue[Long]("epfd.simulation.delay")

  //mutable state
  var period = cfg.getValue[Long]("epfd.simulation.delay")
  var alive = Set(cfg.getValue[List[Address]]("epfd.simulation.topology"): _*)
  var suspected = Set[Address]()
  var seqnum = 0

  def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

  //EPFD event handlers
  ctrl uponEvent {
    case _: Start => handle {
      startTimer(period)
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      if (alive.intersect(suspected).nonEmpty) {
        // Intersection between known alive and suspected processes is non-empty.
        suspected --= alive
      }

      seqnum += 1

      for (p <- topology) {
        if (!alive.contains(p) && !suspected.contains(p)) {
          // We don't know that process p is alive, and process p is not suspected.
          // Therefore we should start suspecting process p.
          suspected += p
          trigger(Suspect(p) -> epfd)
        } else if (alive.contains(p) && suspected.contains(p)) {
          suspected -= p
          trigger(Restore(p) -> epfd)
        }
        trigger(Send(p, HeartbeatRequest(seqnum)) -> pl)
      }
      alive = Set[Address]()
      startTimer(period)
    }
  }

  pl uponEvent {
    case Deliver(src, HeartbeatRequest(seq)) => handle {
      trigger(Send(src, HeartbeatReply(seq)) -> pl)
    }
    case Deliver(src, HeartbeatReply(seq)) => handle {
      alive += src
    }
  }

}