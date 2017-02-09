package se.kth.id2203.broadcasting

import se.kth.id2203.broadcasting.EagerReliableBroadcast._
import se.kth.id2203.ports._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network._
import se.sics.kompics.sl._

object EagerReliableBroadcast {

  case class OriginatedData(src: Address, payload: KompicsEvent) extends KompicsEvent

}

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition {

  val beb = requires[BestEffortBroadcast]
  val rb = provides[ReliableBroadcast]

  val (self, delivered) = init match {
    case Init(self: Address) =>
      (self, collection.mutable.Set[KompicsEvent]())
  }

  rb uponEvent {
    case RB_Broadcast(payload) => handle {
      trigger(BEB_Broadcast(OriginatedData(self, payload)), beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(_, data@OriginatedData(origin, payload)) => handle {
      if (delivered add payload) {
        trigger(RB_Deliver(origin, payload), rb)
        trigger(BEB_Broadcast(data), beb)
      }
    }
  }

}
