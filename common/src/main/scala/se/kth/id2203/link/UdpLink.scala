package se.kth.id2203.link

import se.kth.id2203.{FLL_Deliver, FLL_Send, FairLossLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

class UdpLink(init: Init[UdpLink]) extends ComponentDefinition {

  val fll = provides(FairLossLink)
  val net = requires[Network]

  val self = init match {
    case Init(self: Address) => self
  }

  fll uponEvent {
    case FLL_Send(dest, payload) => handle {
      trigger(NetworkMessage(self, dest, Transport.UDP, payload) -> net)
    }
  }

  net uponEvent {
    case NetworkMessage(src, _, _, payload) => handle {
      trigger(FLL_Deliver(src, payload) -> fll)
    }
  }

}