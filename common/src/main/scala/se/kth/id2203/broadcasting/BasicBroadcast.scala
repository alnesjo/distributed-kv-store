package se.kth.id2203.broadcasting

import se.kth.id2203.ports._
import se.sics.kompics.network._
import se.sics.kompics.sl._

class BasicBroadcast(init: Init[BasicBroadcast]) extends ComponentDefinition {

  val pl = requires[PerfectLink]
  val beb = provides[BestEffortBroadcast]

  val (self, topology) = init match {
    case Init(s: Address, t: Set[Address]@unchecked) => (s, t)
  }

  beb uponEvent {
    case x: BEB_Broadcast => handle {
      for (p <- topology) {
        trigger(PL_Send(p,x), pl)
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(src, BEB_Broadcast(payload)) => handle {
      trigger(BEB_Deliver(src, payload), beb)
    }
  }

}
