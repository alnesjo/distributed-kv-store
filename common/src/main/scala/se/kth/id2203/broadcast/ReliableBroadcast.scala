package se.kth.id2203.broadcast

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

class ReliableBroadcast extends Port {

  indication[RB_Deliver]
  request[RB_Broadcast]

}

case class RB_Deliver(source: Address, payload: KompicsEvent) extends KompicsEvent

case class RB_Broadcast(payload: KompicsEvent) extends KompicsEvent
