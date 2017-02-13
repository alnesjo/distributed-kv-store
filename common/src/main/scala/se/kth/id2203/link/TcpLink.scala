package se.kth.id2203.link

import se.kth.id2203.{Deliver, Send, PerfectLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._


class TcpLink(init: Init[TcpLink]) extends ComponentDefinition {

  val pl = provides(PerfectLink)
  val net = requires[Network]

  val self = init match {
    case Init(self: Address) => self
  }

  pl uponEvent {
    case Send(dest, payload) => handle {
      trigger(NetworkMessage(self, dest, Transport.TCP, payload), net)
    }
  }

  net uponEvent {
    case NetworkMessage(src, _, _, payload) => handle {
      trigger(Deliver(src, payload), pl)
    }
  }

}
