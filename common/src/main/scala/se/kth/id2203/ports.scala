package se.kth.id2203

import se.kth.id2203.events.{Broadcast, Deliver, Send}
import se.sics.kompics.sl.Port

object ports {

  class PerfectLink extends Port {
    indication[Deliver]
    request[Send]
  }

  class BestEffortBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

  class ReliableBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

  class CausalOrderReliableBroadcast extends Port {
    indication[Deliver]
    request[Broadcast]
  }

}
