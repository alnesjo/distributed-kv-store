package se.kth.id2203.failure

import se.sics.kompics.KompicsEvent

case class HeartbeatReply(seq: Int) extends KompicsEvent