package se.kth.id2203

import se.sics.kompics.KompicsEvent

package object shared {

  case class READ(rid: Int) extends KompicsEvent
  case class VALUE(rid: Int, ts: Int, wr: Int, value: Option[Any]) extends KompicsEvent
  case class WRITE(rid: Int, ts: Int, wr: Int, writeVal: Option[Any]) extends KompicsEvent
  case class ACK(rid: Int) extends KompicsEvent

  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)

}
