package se.kth

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

package object id2203 {

  case class FLL_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent
  case class FLL_Send(dst: Address, payload: KompicsEvent) extends KompicsEvent

  class FairLossLink extends Port {
    indication[FLL_Deliver]
    request[FLL_Send]
  }

  case class SL_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent
  case class SL_Send(dst: Address, payload: KompicsEvent) extends KompicsEvent

  class StubbornLink extends Port {
    indication[SL_Deliver]
    request[SL_Send]
  }

  case class PL_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent
  case class PL_Send(dst: Address, payload: KompicsEvent) extends KompicsEvent

  class PerfectLink extends Port {
    indication[PL_Deliver]
    request[PL_Send]
  }

  case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent
  case class BEB_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

  class BestEffortBroadcast extends Port {
    indication[BEB_Deliver]
    request[BEB_Broadcast]
  }

  case class RB_Broadcast(payload: KompicsEvent) extends KompicsEvent
  case class RB_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

  class ReliableBroadcast extends Port {
    indication[RB_Deliver]
    request[RB_Broadcast]
  }

  case class CO_Broadcast(payload: KompicsEvent) extends KompicsEvent
  case class CO_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

  class CausalOrderReliableBroadcast extends Port {
    indication[CO_Deliver]
    request[CO_Broadcast]
  }

  case class EP_Suspect(process: Address) extends KompicsEvent
  case class EP_Restore(process: Address) extends KompicsEvent

  class EventuallyPerfectFailureDetector extends Port {
    indication[EP_Suspect]
    indication[EP_Restore]
  }

  case class AR_Read_Invoke() extends KompicsEvent
  case class AR_Read_Respond(value: Option[Any]) extends KompicsEvent
  case class AR_Write_Invoke(value: Any) extends KompicsEvent
  case class AR_Write_Respond() extends KompicsEvent

  class AtomicRegister extends Port {
    request[AR_Read_Invoke]
    request[AR_Write_Invoke]
    indication[AR_Read_Respond]
    indication[AR_Write_Respond]
  }

}
