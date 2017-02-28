package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier
import se.sics.kompics.KompicsEvent

case class OperationInvoke(id: Identifier, key: String) extends KompicsEvent
