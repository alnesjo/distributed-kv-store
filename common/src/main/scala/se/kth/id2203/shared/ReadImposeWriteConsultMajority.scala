package se.kth.id2203.shared

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.sics.kompics.network._
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}
import se.sics.kompics.sl._

object ReadImposeWriteConsultMajority {

  case class Init(self: Address, nrNodes: Int) extends se.sics.kompics.Init[ReadImposeWriteConsultMajority]

}

class ReadImposeWriteConsultMajority(init: ReadImposeWriteConsultMajority.Init) extends ComponentDefinition {

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

  case class Read(rid: Int) extends KompicsEvent
  case class Value(rid: Int, ts: Int, wr: Int, value: Option[Any]) extends KompicsEvent
  case class Write(rid: Int, ts: Int, wr: Int, writeVal: Option[Any]) extends KompicsEvent
  case class Ack(rid: Int) extends KompicsEvent

  val log = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajority])

  val nnar = provides[AtomicRegister]
  val pl = requires[PerfectLink]
  val beb = requires[BestEffortBroadcast]

  val self = init.self
  val nrNodes = init.nrNodes
  val selfRank = self.##

  var (ts,wr) = (0,0)
  var value: Option[Any] = None
  var acks = 0
  var readval: Option[Any] = None
  var writeval: Option[Any] = None
  var rid = 0
  var readlist = Map.empty[Address, (Int, Int, Option[Any])]
  var reading = false

  nnar uponEvent {
    case AR_Read_Invoke() => handle {
      log.debug(s"Process $self requests to read at $rid")
      rid += 1
      acks = 0
      readlist = Map.empty
      reading = true
      trigger(BEB_Broadcast(Read(rid)) -> beb)
    };
    case AR_Write_Invoke(wval) => handle {
      log.debug(s"Process $self requests to write $wval at $rid")
      rid += 1
      writeval = Some(wval)
      acks = 0
      readlist = Map.empty
      trigger(BEB_Broadcast(Read(rid)) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(src, Read(r)) => handle {
      log.debug(s"Process $self sent VALUE to $src")
      trigger(PL_Send(src, Value(r, ts, wr, value)) -> pl)
    }
    case BEB_Deliver(src, w: Write) => handle {
      if ((w.ts, w.wr) > (ts, wr)) {
        ts = w.ts
        wr = w.wr
        value = w.writeVal
      }
      trigger(PL_Send(src, Ack(w.rid)) -> pl)
    }
  }

  pl uponEvent {
    case PL_Deliver(src, v: Value) => handle {
      if (v.rid == rid) {
        readlist += (src -> (v.ts, v.wr, v.value))
        if (readlist.size > nrNodes/2) {
          val highest = readlist.values.toList.sortWith((l,r) => (l._1, l._2) < (r._1, r._2)).last
          var (maxts, rr) = (highest._1, highest._2)
          readval = highest._3
          val bcastval = if (reading) {
            readval
          } else {
            rr = selfRank
            maxts += 1
            writeval
          }
          log.debug(s"Process $self broadcasts WRITE")
          trigger(BEB_Broadcast(Write(rid, maxts, rr, bcastval)) -> beb)
        }
      }
    }
    case PL_Deliver(src, v: Ack) => handle {
      if (v.rid == rid) {
        log.debug(s"Process $self recieved ACK from $src")
        acks += 1
        if (acks > nrNodes/2) {
          acks = 0
          if (reading) {
            reading = false
            log.debug(s"Process $self successfully read $readval")
            trigger(AR_Read_Respond(readval) -> nnar)
          } else {
            log.debug(s"Process $self successfully wrote")
            trigger(AR_Write_Respond() -> nnar)
          }
        }
      }
    }
  }

}