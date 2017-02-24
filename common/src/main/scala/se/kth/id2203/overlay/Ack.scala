package se.kth.id2203.overlay

import java.util.UUID

import se.sics.kompics.KompicsEvent

case class Ack(id: UUID, clusterSize: Int) extends KompicsEvent
