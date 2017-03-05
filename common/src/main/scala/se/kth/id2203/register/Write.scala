package se.kth.id2203.register

import se.sics.kompics.KompicsEvent

case class Write(rid: Int, ts: Int, wr: Int, writeVal: Option[Any]) extends KompicsEvent