package se.kth.id2203.link

import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

object TcpLink {

  case class Init(self: Address) extends se.sics.kompics.Init[TcpLink]

}

class TcpLink(init: TcpLink.Init) extends ComponentDefinition {

  val pl = provides(PerfectLink)
  val net = requires[Network]

  val self = init.self

  pl uponEvent {
    case PL_Send(dest, payload) => handle {
      trigger(NetworkMessage(self, dest, Transport.TCP, payload) -> net)
    }
  }

  net uponEvent {
    case NetworkMessage(src, _, _, payload) => handle {
      trigger(PL_Deliver(src, payload) -> pl)
    }
  }

}
