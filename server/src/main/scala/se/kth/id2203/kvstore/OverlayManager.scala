package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.kth.id2203.kvstore._
import se.sics.kompics.network.Address
import se.sics.kompics.sl.{ComponentDefinition, _}

import scala.util.Random

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
  var lut = init.lut

  var pending = List.empty[Invocation]
  var current = Option.empty[Invocation]

  pl uponEvent {
    case PL_Deliver(_, Connect(id)) => handle {
      log.info(s"Acknowledging connection request from ${id.src}.")
      trigger(PL_Send(id.src, Ack(id, lut.getNodes.size)) -> pl)
    }
    case PL_Deliver(src, inv: Invocation) => handle {
      val group = lut.lookup(inv.key)
      if (group contains self) {
        log.info(s"Handling operation invocation on key ${inv.key}")
        current match {
          case Some(_) =>
            log.trace("Invocation added to pending queue.")
            pending :+= inv
          case None =>
            log.trace("Addressing invocation immediately.")
            inv match {
              case get: GetInvoke =>
                current = Some(get)
                trigger(AR_Read_Invoke() -> ar)
              case put: PutInvoke =>
                current = Some(put)
                trigger(AR_Read_Invoke() -> ar)
              case _ =>
                trigger(PL_Send(inv.id.src, NotImplemented(inv.id)) -> pl)
            }
        }
      } else {
        log.info(s"Forwarding operation invocation on key ${inv.key}")
        Random.shuffle(group.toList).headOption match {
          case Some(dst) =>
            trigger(PL_Send(dst, inv) -> pl)
          case None =>
            // Entire replication group is suspected by FD, terminate?
            ???
        }

      }
    }
  }

  ar uponEvent {
    case AR_Read_Respond(readval) => handle {
      current match {
        case Some(GetInvoke(id, key)) =>
          val value = readval match {
            case Some(partition: Partition) =>
              partition.get(key)
            case _ =>
              log.debug("Partition is not initialized.")
              None
          }
          log.debug(s"Read value $value with key $key.")
          current = pending.headOption
          pending = pending.drop(1)
          trigger(PL_Send(id.src, GetRespond(id, value)) -> pl)
        case Some(put@PutInvoke(id, key, value)) =>
          val partition = readval match {
            case Some(p: Partition) => p
            case _ =>
              log.debug("Initializing partition store.")
              Partition(Map.empty[String, String])
          }
          trigger(AR_Write_Invoke(partition + (key -> value)) -> ar)
        case _ =>
          log.error("Received read response from atomic register but have no current invocation.")
          System.exit(-1)
      }
    }
    case AR_Write_Respond() => handle {
      current match {
        case Some(PutInvoke(id, key, value)) =>
          log.debug(s"Associated key $key with value $value.")
          current = pending.headOption
          pending = pending.drop(1)
          trigger(PL_Send(id.src, PutRespond(id)) -> pl)
        case _ =>
          log.error("Received write response from atomic register but current invocation is not put.")
          System.exit(-1)
      }
    }
  }

  epfd uponEvent {
    case EP_Suspect(node: Address) => handle {
      lut -= node
    }
    case EP_Restore(node: Address) => handle {
      lut += node
    }
  }

}