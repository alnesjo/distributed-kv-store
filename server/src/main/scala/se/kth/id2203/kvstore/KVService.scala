package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203.kvstore.OperationRespond._
import se.kth.id2203.link.NetworkMessage
import se.kth.id2203.overlay.Routing
import se.sics.kompics.sl._
import se.sics.kompics.network.{Address, Network, Transport}

object KVService {

  case class Init(self: Address) extends se.sics.kompics.Init[KVService]

}

class KVService(init: KVService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[KVService])

  val net = requires[Network]
  val route = requires(Routing)

  val self = init.self

  net uponEvent {
    case NetworkMessage(src, _, _, content: OperationInvoke) => handle {
      log.info("Got operation {}! Now implement me please :)", content)
      trigger(NetworkMessage(self, src, Transport.TCP, OperationRespond(content.id, NotImplemented)) -> net)
    }
  }

}