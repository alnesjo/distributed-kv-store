package se.kth.id2203.overlay

import se.sics.kompics.KompicsEvent

case class RouteMessage(key: String, message: KompicsEvent) extends KompicsEvent
