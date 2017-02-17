package se.kth.id2203

import se.sics.kompics.KompicsEvent

package object shared {

  case class Read(rid: Int) extends KompicsEvent
  case class Value(rid: Int, ts: Int, wr: Int, value: Option[Any]) extends KompicsEvent
  case class Write(rid: Int, ts: Int, wr: Int, writeVal: Option[Any]) extends KompicsEvent
  case class Ack(rid: Int) extends KompicsEvent

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

}
