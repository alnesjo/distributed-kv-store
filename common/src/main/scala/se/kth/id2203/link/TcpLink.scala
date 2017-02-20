package se.kth.id2203.link

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

object TcpLink {

  case class Init(self: Address) extends se.sics.kompics.Init[TcpLink]

}

class TcpLink(init: TcpLink.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[TcpLink])

  val pl = provides(PerfectLink)
  val net = requires[Network]

  val self = init.self

  pl uponEvent {
    case e@PL_Send(dst, payload) => handle {
      log.debug(s"Handling request $e on PerfectLink")
      trigger(new NetworkMessage(self, dst, Transport.TCP, payload) -> net)
    }
  }

  net uponEvent {
    case e: NetworkMessage => handle {
      log.debug(s"Handling indication $e on Network")
      trigger(PL_Deliver(e.getSource, e.payload) -> pl)
    }
  }

}
