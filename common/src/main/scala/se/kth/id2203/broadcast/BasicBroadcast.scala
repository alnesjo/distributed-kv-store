package se.kth.id2203.broadcast

import se.kth.id2203.{Broadcast, Deliver, Send, BestEffortBroadcast, PerfectLink}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class BasicBroadcast(init: Init[BasicBroadcast]) extends ComponentDefinition {

  val pl = requires(PerfectLink)
  val beb = provides(BestEffortBroadcast)

  val (self, topology) = init match {
    case Init(s: Address, t: Set[Address]@unchecked) => (s, t)
  }

  beb uponEvent {
    case Broadcast(payload) => handle {
      for (p <- topology) {
        trigger(Send(p,payload), pl)
      }
    }
  }

  pl uponEvent {
    case Deliver(src, payload) => handle {
      trigger(Deliver(src, payload), beb)
    }
  }

}
