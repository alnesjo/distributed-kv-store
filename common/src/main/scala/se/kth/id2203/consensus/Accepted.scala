package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Accepted(acceptedBallot: (Int, Int)) extends KompicsEvent