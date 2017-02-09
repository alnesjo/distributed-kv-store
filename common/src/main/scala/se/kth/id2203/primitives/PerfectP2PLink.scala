package se.kth.id2203.primitives

import se.kth.id2203.networking._
import se.kth.id2203.ports._
import se.sics.kompics.sl._
import se.sics.kompics.network._


class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {

  val pl = provides[PerfectLink]
  val network = requires[Network]

  val self = init match {
    case Init(self: Address) => self
  }

  pl uponEvent {
    case PL_Send(dest, payload) => handle {
      trigger(NetMessage(self, dest, Transport.TCP, payload) -> network)
    }
  }

  network uponEvent {
    case NetMessage(src, _, _, payload) => handle {
      trigger(PL_Deliver(src, payload) -> pl)
    }
  }

}
