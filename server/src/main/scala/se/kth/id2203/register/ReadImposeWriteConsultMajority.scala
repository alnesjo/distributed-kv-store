package se.kth.id2203.register

import org.slf4j.LoggerFactory
import se.kth.id2203._
import se.kth.id2203.kvstore.LookupTable
import se.sics.kompics.network._
import se.sics.kompics.sl._

import scala.collection.mutable

object ReadImposeWriteConsultMajority {

  case class Init(self: Address, lut: LookupTable) extends se.sics.kompics.Init[ReadImposeWriteConsultMajority]

}

class ReadImposeWriteConsultMajority(init: ReadImposeWriteConsultMajority.Init) extends ComponentDefinition {

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

  val log = LoggerFactory.getLogger(classOf[ReadImposeWriteConsultMajority])

  val ar = provides[PartitionedAtomicRegister]
  val pl = requires[PerfectLink]
  val beb = requires[BestEffortBroadcast]

  val self = init.self
  val selfRank = self.##
  val partitions = init.lut.partitions(self)
  val replicators: Map[Int, Set[Address]] = init.lut.replicators.filterKeys(partitions)

  var ts: mutable.Map[Int, Int] = mutable.Map(partitions.map(_ -> 0).toSeq: _*)
  var wr: mutable.Map[Int, Int] = mutable.Map(partitions.map(_ -> 0).toSeq: _*)
  var value: mutable.Map[Int, Option[Any]] = mutable.Map(partitions.map(_ -> None).toSeq: _*)
  var acks: mutable.Map[Int, Int] = mutable.Map(partitions.map(_ -> 0).toSeq: _*)
  var readval: mutable.Map[Int, Option[Any]] = mutable.Map(partitions.map(_ -> None).toSeq: _*)
  var writeval: mutable.Map[Int, Option[Any]] = mutable.Map(partitions.map(_ -> None).toSeq: _*)
  var rid: mutable.Map[Int, Int] = mutable.Map(partitions.map(_ -> 0).toSeq: _*)
  var readlist: mutable.Map[Int, Map[Address, (Int, Int, Option[Any])]] =
    mutable.Map(partitions.map(_ -> Map.empty[Address, (Int, Int, Option[Any])]).toSeq: _*)
  var reading: mutable.Map[Int, Boolean] = mutable.Map(partitions.map(_ -> false).toSeq: _*)

  ar uponEvent {
    case PAR_Read_Invoke(i) if partitions(i) => handle {
      rid(i) += 1
      log.trace(s"$selfRank Read Invoke")
      acks(i) = 0
      readlist(i) = Map.empty
      reading(i) = true
      trigger(BEB_Broadcast(Read(i, rid(i))) -> beb)

    }
    case PAR_Write_Invoke(i, v) if partitions(i) => handle {
      rid(i) += 1
      log.trace(s"$selfRank Write Invoke: $v")
      writeval(i) = Some(v)
      acks(i) = 0
      readlist(i) = Map.empty
      trigger(BEB_Broadcast(Read(i, rid(i))) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(p, Read(i, r)) if partitions(i) => handle {
      trigger(PL_Send(p, Value(i, r, ts(i), wr(i), value(i))) -> pl)
    }
    case BEB_Deliver(p, Write(i, r, ts1, wr1, v1)) if partitions(i) => handle {
      if ((ts1, wr1) > (ts(i), wr(i))) {
        ts(i) = ts1
        wr(i) = wr1
        value(i) = v1
      }
      trigger(PL_Send(p, Ack(i, r)) -> pl)
    }
  }

  pl uponEvent {
    case PL_Deliver(p, Value(i, r, ts1, wr1, v1)) if r == rid(i) => handle {
      readlist(i) += (p -> (ts1, wr1, v1))
      if (readlist(i).size > replicators(i).size/2) {
        val highest = readlist(i).values.toList.sortBy(x => (x._1, x._2)).last
        readlist(i) = Map.empty
        var (maxts, rr) = (highest._1, highest._2)
        readval(i) = highest._3
        val bcastval = if (reading(i)) {
          readval(i)
        } else {
          rr = selfRank
          maxts += 1
          writeval(i)
        }
        trigger(BEB_Broadcast(Write(i, rid(i), maxts, rr, bcastval)) -> beb)
        }
    }

    case PL_Deliver(p, Ack(i, r)) if r == rid(i) => handle {
      acks(i) += 1
      if (acks(i) > replicators(i).size/2) {
        acks(i) = 0
        if (reading(i)) {
          log.trace(s"$selfRank Read Respond: ${readval(i)}")
          reading(i) = false
          trigger(PAR_Read_Respond(i, readval(i)) -> ar)
        } else {
          log.trace(s"$selfRank Write Respond")
          trigger(PAR_Write_Respond(i) -> ar)
        }
      }
    }
  }

}