package se.kth.id2203.register

import se.sics.kompics.KompicsEvent

case class Ack(rid: Int) extends KompicsEvent
