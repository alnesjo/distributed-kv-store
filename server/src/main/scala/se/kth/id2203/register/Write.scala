package se.kth.id2203.register

import se.sics.kompics.KompicsEvent

case class Write(partition: Int, rid: Int, ts: Int, wr: Int, writeVal: Option[Any]) extends KompicsEvent