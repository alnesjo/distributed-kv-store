package se.kth.id2203.kvstore

import java.util.UUID

import se.sics.kompics.KompicsEvent

object OperationRespond {

  sealed trait Code
  case object Ok extends Code
  case object NotFound extends Code
  case object NotImplemented extends Code

}

case class OperationRespond(id: UUID, status: OperationRespond.Code) extends KompicsEvent {

  override def toString = super.toString

}