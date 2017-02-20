package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.kvstore.OperationRespond._
import se.kth.id2203.overlay.Routing
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
    case PL_Deliver(src, op@OperationInvoke(key)) => handle {
      log.info("Got operation {}! Now implement me please :)", op)
      trigger(PL_Send(src, OperationRespond(op.id, NotImplemented)) -> pl)
    }
  }

}
