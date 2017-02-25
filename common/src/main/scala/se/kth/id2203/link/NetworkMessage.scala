package se.kth.id2203.link

import se.sics.kompics.KompicsEvent
import se.sics.kompics.PatternExtractor
import se.sics.kompics.network.Address
import se.sics.kompics.network.Msg
import se.sics.kompics.network.Transport

case class NetworkMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
  extends Msg[Address, NetworkHeader] with PatternExtractor[Class[_ <: KompicsEvent], KompicsEvent] {

  val header: NetworkHeader = new NetworkHeader(src, dst, ptc)

  override def getHeader: NetworkHeader = this.header

  override def getSource: Address = this.header.src

  override def getDestination: Address = this.header.dst

  override def getProtocol: Transport = this.header.ptc

  override def extractPattern: Class[_ <: KompicsEvent] = payload.getClass

  override def extractValue: KompicsEvent = payload

  override def toString: String = s"$getClass($getSource,$getDestination,$getProtocol,$payload)"

}