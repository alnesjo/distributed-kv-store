package se.kth.id2203.overlay

import se.sics.kompics.KompicsEvent

case class Ack(id: Identifier, clusterSize: Int) extends KompicsEvent
