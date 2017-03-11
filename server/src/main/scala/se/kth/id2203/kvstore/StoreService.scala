package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.kth.id2203.register._
import se.sics.kompics.network.Address
import se.sics.kompics.sl.{ComponentDefinition, _}

import scala.util.Random

object StoreService {

  /**
    * @param self Address of self
    */
  case class Init(self: Address, lut: LookupTable) extends se.sics.kompics.Init[StoreService]

}

/**
  * Handles store operations on one partition.
 *
  * @see [[se.kth.id2203.kvstore.StoreService.Init Init]]
  */
class StoreService(init: StoreService.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[StoreService])

  val kvs = provides[KeyValueStore]
  val ar = requires[PartitionedAtomicRegister]

  val self = init.self
  val lut = init.lut

  var pending = List.empty[Invocation]
  var current = Option.empty[Invocation]

  kvs uponEvent {
    case inv: Invocation => handle {
      current match {
        case Some(_) =>
          log.trace("Invocation added to pending queue.")
          pending :+= inv
        case None =>
          log.trace("Addressing invocation immediately.")
          inv match {
            case get: GetInvoke =>
              current = Some(get)
              trigger(PAR_Read_Invoke(lut.lookup(get.key)) -> ar)
            case put: PutInvoke =>
              current = Some(put)
              trigger(PAR_Read_Invoke(lut.lookup(put.key)) -> ar)
            case _ =>
              trigger(NotImplemented(inv.id) -> kvs)
          }
      }
    }
  }

  ar uponEvent {
    case PAR_Read_Respond(i, readval) => handle {
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
          trigger(GetRespond(id, value) -> kvs)
        case Some(put@PutInvoke(id, key, value)) =>
          val partition = readval match {
            case Some(p: Partition) => p
            case _ =>
              log.debug("Initializing partition store.")
              Partition(Map.empty[String, String])
          }
          trigger(PAR_Write_Invoke(lut.lookup(put.key), partition + (key -> value)) -> ar)
        case _ =>
          log.error("Received read response from atomic register but have no current invocation.")
          System.exit(-1)
      }
    }
    case PAR_Write_Respond(i) => handle {
      current match {
        case Some(PutInvoke(id, key, value)) =>
          log.debug(s"Associated key $key with value $value.")
          current = pending.headOption
          pending = pending.drop(1)
          trigger(PutRespond(id) -> kvs)
        case _ =>
          log.error("Received write response from atomic register but current invocation is not put.")
          System.exit(-1)
      }
    }
  }



}