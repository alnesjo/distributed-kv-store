package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.kvstore.OperationRespond._
import se.kth.id2203.overlay.{Identifier, Routing}
import se.sics.kompics.sl._
import se.sics.kompics.network.Address

object KVService {

  case class Init(self: Address) extends se.sics.kompics.Init[KVService]

}

class KVService(init: KVService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[KVService])

  val pl = requires(PerfectLink)
  val route = requires(Routing)

  val self = init.self

  pl uponEvent {
    case PL_Deliver(_, op@OperationInvoke(id, key)) => handle {
      log.info(s"Got operation $op! Now implement me please :)")
      trigger(PL_Send(id.src, OperationRespond(id, NotImplemented)) -> pl)
    }
  }

}
