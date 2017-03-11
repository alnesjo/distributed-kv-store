package se.kth.id2203.register

import se.sics.kompics.KompicsEvent

case class Ack(partition: Int, rid: Int) extends KompicsEvent
