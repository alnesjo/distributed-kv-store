package se.kth.id2203

import se.sics.kompics.KompicsEvent

package object kvstore {

  case class GetInvocation(key: Int) extends KompicsEvent
  case class GetResponse(value: String) extends KompicsEvent

  case class PutInvocation(key: Int, value: String) extends KompicsEvent
  case class PutResponse(status: Status) extends KompicsEvent

  sealed trait Status
  case object Success extends Status
  case object Failure extends Status

}
