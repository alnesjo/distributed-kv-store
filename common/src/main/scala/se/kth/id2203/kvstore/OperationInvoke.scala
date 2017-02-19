package se.kth.id2203.kvstore

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class OperationInvoke(key: String) extends KompicsEvent {

  val id = UUID.randomUUID

  override def toString = super.toString

}
