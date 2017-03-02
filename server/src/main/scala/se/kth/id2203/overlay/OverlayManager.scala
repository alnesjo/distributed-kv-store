package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.kth.id2203.kvstore._
import se.sics.kompics.network.Address
import se.sics.kompics.sl.{ComponentDefinition, _}

object OverlayManager {

  case class Init(self: Address, lut: LookupTable) extends se.sics.kompics.Init[OverlayManager]

}

class OverlayManager(init: OverlayManager.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[OverlayManager])

  val pl = requires[PerfectLink]
  val epfd = requires[EventuallyPerfectFailureDetector]
  val beb = requires[BestEffortBroadcast] // Across entire cluster, might not be needed
  val ar = requires[AtomicRegister]

  val self = init.self
  val lut = init.lut

  var pending = List.empty[Invocation]

  pl uponEvent {
    case PL_Deliver(_, Connect(id)) => handle {
      log.info(s"Acknowledging connection request from ${id.src}.")
      trigger(PL_Send(id.src, Ack(id, lut.getNodes.size)) -> pl)
    }
    case PL_Deliver(src, inv: Invocation) => handle {
      val group = lut.lookup(inv.key)
      if (group contains self) {
        log.info(s"Handling operation invocation on key ${inv.key}")
        inv match {
          case get: GetInvoke =>
            pending :+= get
            log.trace("Get invocation added to pending queue.")
            trigger(AR_Read_Invoke() -> ar)
          case put: PutInvoke =>
            pending :+= put
            log.trace("Put invocation added to pending queue.")
            trigger(AR_Read_Invoke() -> ar)
          case _ =>
            trigger(PL_Send(inv.id.src, NotImplemented(inv.id)) -> pl)
        }
      } else {
        log.info(s"Forwarding operation invocation on key ${inv.key}")
        for (p <- group) trigger(PL_Send(p, inv) -> pl)
      }
    }
  }

  ar uponEvent {
    case AR_Read_Respond(readval) => handle {
      pending match {
        case (GetInvoke(id, key)) :: tail =>
          pending = tail
          readval match {
            case Some(partition: Map[String, String]) => // Unchecked
              val value = partition.get(key)
              log.debug(s"Read value $value with key $key.")
              trigger(PL_Send(id.src, GetRespond(id, value)) -> pl)
            case _ =>
              log.debug("Partition store not initialized.")
              trigger(PL_Send(id.src, GetRespond(id, None)) -> pl)
          }
        case (put@PutInvoke(id, key, value)) :: tail =>
          pending = tail :+ put
          log.trace("Put invocation re-queued.")
          readval match {
            case Some(partition: Map[String, String]) => // Unchecked
              trigger(AR_Write_Invoke(partition + (key -> value)) -> ar)
            case _ =>
              log.debug("Initializing partition store.")
              trigger(AR_Write_Invoke(Map.empty + (key -> value)) -> ar)
          }
        case _ =>
          log.error("Received read response from atomic register but have no pending invocations.")
          System.exit(-1)
      }
    }
    case AR_Write_Respond() => handle {
      pending match {
        case (PutInvoke(id, key, value)) :: tail =>
          log.debug(s"Associated key $key with value $value.")
          pending = tail
          trigger(PL_Send(id.src, PutRespond(id)) -> pl)
        case _ =>
          log.error("Received write response from atomic register but pending request was not a put invocation.")
          System.exit(-1)
      }
    }
  }

}