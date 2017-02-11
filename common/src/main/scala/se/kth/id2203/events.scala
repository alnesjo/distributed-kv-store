package se.kth.id2203

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address

object events {

  case class Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

  case class Send(dst: Address, payload: KompicsEvent) extends KompicsEvent

  case class Broadcast(payload: KompicsEvent) extends KompicsEvent

}