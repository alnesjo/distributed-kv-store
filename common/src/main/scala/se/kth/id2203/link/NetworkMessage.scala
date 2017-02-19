package se.kth.id2203.link

import se.kth.id2203.overlay.Connect
import se.sics.kompics.{KompicsEvent, PatternExtractor}
import se.sics.kompics.network.{Address, Msg, Transport}

case class NetworkMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
  extends Msg[Address, NetworkHeader] with Serializable with PatternExtractor[Class[_ <: KompicsEvent], KompicsEvent] {

  val header = NetworkHeader(src, dst, ptc)

  override def getHeader = header

  override def getSource = header.getSource

  override def getDestination = header.getDestination

  override def getProtocol = header.getProtocol

  override def extractValue = payload

  override def extractPattern = payload.getClass
}