package se.kth.id2203.broadcast

import se.kth.id2203._
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class BasicBroadcast(init: Init[BasicBroadcast]) extends ComponentDefinition {

  val pl = requires(PerfectLink)
  val beb = provides(BestEffortBroadcast)

  val (self, topology) = init match {
    case Init(s: Address, t: Set[Address]@unchecked) => (s, t)
  }

  beb uponEvent {
    case BEB_Broadcast(payload) => handle {
      for (p <- topology) {
        trigger(PL_Send(p,payload) -> pl)
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(src, payload) => handle {
      trigger(BEB_Deliver(src, payload) -> beb)
    }
  }

}
