package se.kth.id2203.shared

import se.kth.id2203._
import se.sics.kompics.network._
import se.sics.kompics.sl._
import se.sics.kompics.{ComponentDefinition => _, Port => _}
import scala.collection.mutable

class ReadImposeWriteConsultMajority(init: Init[ReadImposeWriteConsultMajority]) extends ComponentDefinition {

  val nnar = provides(AtomicRegister)
  val pl = requires(PerfectLink)
  val beb = requires(BestEffortBroadcast)

  val (self: Address, n: Int) = init match {case Init(selfAddr: Address, n: Int) => (selfAddr, n)}
  val selfRank = self.getIp.getAddress map (x => if (x < 0) x + 256 else x) apply 3

  var (ts, wr) = (0, 0)
  var value: Option[Any] = None
  var acks = 0
  var readval: Option[Any] = None
  var writeval: Option[Any] = None
  var rid = 0
  var readlist: mutable.Map[Address, (Int, Int, Option[Any])] = mutable.Map.empty
  var reading = false

  nnar uponEvent {
    case AR_Read_Invoke() => handle {
      println(s"Process $self requests to read")
      rid += 1
      acks = 0
      readlist = mutable.Map.empty
      reading = true
      trigger(BEB_Broadcast(READ(rid)) -> beb)
    };
    case AR_Write_Invoke(wval) => handle {
      println(s"Process $self requests to write $wval")
      rid += 1
      writeval = Some(wval)
      acks = 0
      readlist = mutable.Map.empty
      trigger(BEB_Broadcast(READ(rid)) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(src, READ(r)) => handle {
      println(s"Process $self sent VALUE to $src")
      trigger(PL_Send(src, VALUE(r, ts, wr, value)) -> pl)
    }
    case BEB_Deliver(src, w: WRITE) => handle {
      if ((w.ts, w.wr) > (ts, wr)) {
        ts = w.ts
        wr = w.wr
        value = w.writeVal
      }
      trigger(PL_Send(src, ACK(w.rid)) -> pl)
    }
  }

  pl uponEvent {
    case PL_Deliver(src, v: VALUE) => handle {
      if (v.rid == rid) {
        readlist.put(src, (v.ts, v.wr, v.value))
        if (readlist.size > n/2) {
          var (maxts, rr) = readlist.values.toList.sortWith((l, r) => (l._1, l._2) < (r._1, r._2)).last match {
            case (t, r, rv) =>
              readval = rv
              (t, r)
          }
          readlist = mutable.Map.empty
          val bcastval = if (reading) {
            readval
          } else {
            rr = selfRank
            maxts += maxts
            writeval
          }
          println(s"Process $self broadcasts WRITE")
          trigger(BEB_Broadcast(WRITE(rid, maxts, rr, bcastval)) -> beb)
        }
      }
    }
    case PL_Deliver(src, v: ACK) => handle {
      if (v.rid == rid) {
        println(s"Process $self recieved ACK from $src")
        acks += 1
        if (acks > n/2) {
          acks = 0
          if (reading) {
            reading = false
            println(s"Process $self successfully read $readval")
            trigger(AR_Read_Respond(readval) -> nnar)
          } else {
            println(s"Process $self successfully wrote")
            trigger(AR_Write_Respond() -> nnar)
          }
        }
      }
    }
  }
}