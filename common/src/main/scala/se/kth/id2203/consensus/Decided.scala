package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Decided(decidedValue: Any) extends KompicsEvent