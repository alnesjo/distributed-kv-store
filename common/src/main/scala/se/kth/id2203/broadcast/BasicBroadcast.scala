package se.kth.id2203.broadcast

import se.kth.id2203.link.{Deliver, PerfectLink, Send}
import se.sics.kompics.network._
import se.sics.kompics.sl._

class BasicBroadcast(init: Init[BasicBroadcast]) extends ComponentDefinition {

  val pl = requires[PerfectLink]
  val beb = provides[BestEffortBroadcast]

  val (self, topology) = init match {
    case Init(s: Address, t: Set[Address]) => (s, t)
  }

  beb uponEvent {
    case x: Broadcast => handle {
      for (p <- topology) {
        trigger(Send(p,x), pl)
      }
    }
  }

  pl uponEvent {
    case Deliver(src, Broadcast(payload)) => handle {
      trigger(Deliver(src, payload), beb)
    }
  }

}
