package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Prepare(proposalBallot: (Int, Int)) extends KompicsEvent
