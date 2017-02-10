package se.kth.id2203.broadcast

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

class BestEffortBroadcast extends Port {

  indication[BEB_Deliver]
  request[BEB_Broadcast]

}

case class BEB_Deliver(source: Address, payload: KompicsEvent) extends KompicsEvent

case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent
