package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Accept(acceptBallot: (Int, Int), proposedValue: Any) extends KompicsEvent