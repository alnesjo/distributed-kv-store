package se.kth.id2203.failure

import se.sics.kompics.KompicsEvent

case class HeartbeatRequest(seq: Int) extends KompicsEvent
