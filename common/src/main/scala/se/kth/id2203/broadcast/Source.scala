package se.kth.id2203.broadcast

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address

case class Source(src: Address, payload: KompicsEvent) extends KompicsEvent