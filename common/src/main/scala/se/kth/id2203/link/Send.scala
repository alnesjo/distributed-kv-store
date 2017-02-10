package se.kth.id2203.link

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address

case class Send(dst: Address, payload: KompicsEvent) extends KompicsEvent
