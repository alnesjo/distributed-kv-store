package se.kth.id2203.kvstore

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class OperationInvoke(id: String, key: String) extends KompicsEvent {

  override def toString = s"$getClass($id,$key)"

}
