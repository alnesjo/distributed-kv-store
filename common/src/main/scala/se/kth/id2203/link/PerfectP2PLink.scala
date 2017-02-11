package se.kth.id2203.link

import se.kth.id2203.events.{Deliver, Send}
import se.kth.id2203.ports.PerfectLink
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._


class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {

  val pl = provides[PerfectLink]
  val network = requires[Network]

  val self = init match {
    case Init(self: Address) => self
  }

  pl uponEvent {
    case Send(dest, payload) => handle {
      trigger(NetworkMessage(self, dest, Transport.TCP, payload), network)
    }
  }

  network uponEvent {
    case NetworkMessage(src, _, _, payload) => handle {
      trigger(Deliver(src, payload), pl)
    }
  }

}
