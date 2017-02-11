package se.kth.id2203.broadcast

import se.kth.id2203.events.{Broadcast, Deliver}
import se.kth.id2203.ports.{BestEffortBroadcast, ReliableBroadcast}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition {

  val beb = requires[BestEffortBroadcast]
  val rb = provides[ReliableBroadcast]

  val (self, delivered) = init match {
    case Init(s: Address) =>
      (s, collection.mutable.Set[KompicsEvent]())
  }

  rb uponEvent {
    case Broadcast(payload) => handle {
      trigger(Broadcast(Source(self, payload)), beb)
    }
  }

  beb uponEvent {
    case Deliver(_, data@Source(sender, payload)) => handle {
      if (delivered add payload) {
        trigger(Deliver(sender, payload), rb)
        trigger(Broadcast(data), beb)
      }
    }
  }

}