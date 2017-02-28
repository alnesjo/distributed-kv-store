package se.kth.id2203.link

import se.sics.kompics.KompicsEvent
import se.sics.kompics.PatternExtractor
import se.sics.kompics.network.{Address, Header, Msg, Transport}

case class NetworkMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
  extends Msg[Address, Header[Address]] with PatternExtractor[Class[_ <: KompicsEvent], KompicsEvent] {

  override def getHeader: Header[Address] = new NetworkHeader(src, dst, ptc)

  override def getSource: Address = this.src

  override def getDestination: Address = this.dst

  override def getProtocol: Transport = this.ptc

  override def extractPattern: Class[_ <: KompicsEvent] = payload.getClass

  override def extractValue: KompicsEvent = payload

}