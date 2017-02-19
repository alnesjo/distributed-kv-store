package se.kth.id2203.link

import se.kth.id2203.{FLL_Deliver, FLL_Send, FairLossLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

object UdpLink {

  case class Init(self: Address) extends se.sics.kompics.Init[UdpLink]

}

class UdpLink(init: UdpLink.Init) extends ComponentDefinition {

  val fll = provides(FairLossLink)
  val net = requires[Network]

  val self = init.self

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