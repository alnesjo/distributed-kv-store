package se.kth.id2203.broadcast

import se.kth.id2203._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class EagerReliableBroadcast(init: Init[EagerReliableBroadcast]) extends ComponentDefinition {

  val beb = requires(BestEffortBroadcast)
  val rb = provides(ReliableBroadcast)

  val self = init match {case Init(s: Address) => s}
  val delivered = collection.mutable.Set[KompicsEvent]()

  rb uponEvent {
    case RB_Broadcast(payload) => handle {
      trigger(BEB_Broadcast(Source(self, payload)) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(_, data@Source(sender, payload)) => handle {
      if (delivered add payload) {
        trigger(RB_Deliver(sender, payload) -> rb)
        trigger(BEB_Broadcast(data) -> beb)
      }
    }
  }

}