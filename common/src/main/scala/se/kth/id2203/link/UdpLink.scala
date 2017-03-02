package se.kth.id2203.link

import org.slf4j.LoggerFactory
import se.kth.id2203.{FLL_Deliver, FLL_Send, FairLossLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

object UdpLink {

  case class Init(self: Address) extends se.sics.kompics.Init[UdpLink]

}

class UdpLink(init: UdpLink.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[TcpLink])

  val fll = provides[FairLossLink]
  val net = requires[Network]

  val self = init.self

  fll uponEvent {
    case e@FLL_Send(dest, payload) => handle {
      log.trace(s"Handling request $e on $fll")
      trigger(NetworkMessage(self, dest, Transport.UDP, payload) -> net)
    }
  }

  net uponEvent {
    case e@NetworkMessage(src, `self`, Transport.UDP, payload) => handle {
      log.trace(s"Handling indication $e on $net")
      trigger(FLL_Deliver(e.getSource, e.payload) -> fll)
    }
  }

}