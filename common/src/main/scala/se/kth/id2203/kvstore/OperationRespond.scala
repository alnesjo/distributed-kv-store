package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier
import se.sics.kompics.KompicsEvent

object OperationRespond {

  sealed trait Code
  case object Ok extends Code
  case object NotFound extends Code
  case object NotImplemented extends Code

}

case class OperationRespond(id: Identifier, status: OperationRespond.Code) extends KompicsEvent