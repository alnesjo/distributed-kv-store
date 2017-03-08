package se.kth.id2203.consensus

import se.sics.kompics.KompicsEvent

case class Nack(ballot: (Int, Int)) extends KompicsEvent