package se.kth

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

package object id2203 {

  case class Broadcast(payload: KompicsEvent) extends KompicsEvent

  case class Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

  case class Send(dst: Address, payload: KompicsEvent) extends KompicsEvent

  object FairLossLink extends Port {
    indication[Deliver]
    request[Send]
  }

  object StubbornLink extends Port {
    indication[Deliver]
    request[Send]
  }

  object PerfectLink extends Port {
    indication[Deliver]
    request[Send]
  }

  object BestEffortBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

  object ReliableBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

  object CausalOrderReliableBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

}
