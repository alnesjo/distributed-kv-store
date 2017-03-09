package se.kth.id2203.kvstore

import se.sics.kompics.KompicsEvent

case class Ack(id: Identifier, clusterSize: Int) extends KompicsEvent
