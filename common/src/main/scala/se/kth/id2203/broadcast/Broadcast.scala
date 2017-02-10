package se.kth.id2203.broadcast

import se.sics.kompics.KompicsEvent

case class Broadcast(payload: KompicsEvent) extends KompicsEvent