package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier
import se.sics.kompics.KompicsEvent

trait Operation extends KompicsEvent {
  val id: Identifier
}
