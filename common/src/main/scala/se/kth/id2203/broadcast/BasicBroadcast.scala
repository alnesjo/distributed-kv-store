package se.kth.id2203.broadcast

import se.kth.id2203._
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

object BasicBroadcast {

  case class Init(self: Address, topology: Set[Address]@unchecked) extends se.sics.kompics.Init[BasicBroadcast]

}

class BasicBroadcast(init: BasicBroadcast.Init) extends ComponentDefinition {

  val pl = requires(PerfectLink)
  val beb = provides(BestEffortBroadcast)

  val self = init.self
  val topology = init.topology

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
