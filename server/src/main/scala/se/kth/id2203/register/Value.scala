package se.kth.id2203.register

import se.sics.kompics.KompicsEvent

case class Value(partition: Int, rid: Int, ts: Int, wr: Int, value: Option[Any]) extends KompicsEvent