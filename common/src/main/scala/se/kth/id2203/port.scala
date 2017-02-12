package se.kth.id2203

import se.kth.id2203.event._
import se.sics.kompics.sl.Port

object port {

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
