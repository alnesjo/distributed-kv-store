package se.kth.id2203.link

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

class PerfectLink extends Port {

  indication[PL_Deliver]
  request[PL_Send]

}

case class PL_Deliver(src: Address, payload: KompicsEvent) extends KompicsEvent

case class PL_Send(dst: Address, payload: KompicsEvent) extends KompicsEvent