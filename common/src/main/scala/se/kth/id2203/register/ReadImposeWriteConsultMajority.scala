package se.kth.id2203.register

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.kth.id2203.kvstore.Identifier
import se.sics.kompics.network._
import se.sics.kompics.{KompicsEvent, ComponentDefinition => _, Port => _}
import se.sics.kompics.sl._

object ReadImposeWriteConsultMajority {

  case class Init(self: Address, nrNodes: Int) extends se.sics.kompics.Init[ReadImposeWriteConsultMajority]

}

class ReadImposeWriteConsultMajority(init: ReadImposeWriteConsultMajority.Init) extends ComponentDefinition {

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

  val log = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajority])

  val ar = provides[AtomicRegister]
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

  ar uponEvent {
    case AR_Read_Invoke() => handle {
      rid += 1
      log.trace(s"$selfRank Read Invoke")
      acks = 0
      readlist = Map.empty
      reading = true
      trigger(BEB_Broadcast(Read(rid)) -> beb)

    };
    case AR_Write_Invoke(v) => handle {
      rid += 1
      log.trace(s"$selfRank Write Invoke: $v")
      writeval = Some(v)
      acks = 0
      readlist = Map.empty
      trigger(BEB_Broadcast(Read(rid)) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(p, Read(r)) => handle {
      trigger(PL_Send(p, Value(r, ts, wr, value)) -> pl)
    }
    case BEB_Deliver(p, Write(r, ts1, wr1, v1)) => handle {
      if ((ts1, wr1) > (ts, wr)) {
        ts = ts1
        wr = wr1
        value = v1
      }
      trigger(PL_Send(p, Ack(r)) -> pl)
    }
  }

  pl uponEvent {
    case PL_Deliver(p, Value(r, ts1, wr1, v1)) => handle {
      if (r == rid) {
        readlist += (p -> (ts1, wr1, v1))
        if (readlist.size > nrNodes/2) {
          val highest = readlist.values.toList.sortBy(x => (x._1, x._2)).last
          readlist = Map.empty
          var (maxts, rr) = (highest._1, highest._2)
          readval = highest._3
          val bcastval = if (reading) {
            readval
          } else {
            rr = selfRank
            maxts += 1
            writeval
          }
          trigger(BEB_Broadcast(Write(rid, maxts, rr, bcastval)) -> beb)
        }
      }
    }

    case PL_Deliver(p, Ack(r)) => handle {
      if (r == rid) {
        acks += 1
        if (acks > nrNodes/2) {
          acks = 0
          if (reading) {
            log.trace(s"$selfRank Read Respond: $readval")
            reading = false
            trigger(AR_Read_Respond(readval) -> ar)
          } else {
            log.trace(s"$selfRank Write Respond")
            trigger(AR_Write_Respond() -> ar)
          }
        }
      }
    }
  }

}