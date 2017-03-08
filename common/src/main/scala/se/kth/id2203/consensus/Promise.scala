package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Promise(promiseBallot: (Int, Int), acceptedBallot: (Int, Int), acceptedValue: Option[Any]) extends KompicsEvent