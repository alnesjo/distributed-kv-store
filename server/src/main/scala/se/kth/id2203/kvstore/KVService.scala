package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
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
    case NetworkMessage(src, _, _, op@OperationInvoke(id, key)) => handle {
      log.info(s"Got operation $op from $src! Now implement me please :)")
      trigger(NetworkMessage(self, src, Transport.TCP, OperationRespond(id, NotImplemented)) -> net)
    }
  }

}
