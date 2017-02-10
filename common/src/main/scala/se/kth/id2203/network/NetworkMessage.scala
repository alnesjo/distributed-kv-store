package se.kth.id2203.network

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Address, Msg, Transport}

case class NetworkMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
  extends Msg[NetworkAddress, NetworkHeader] with Serializable {

  val header = NetworkHeader(src, dst, ptc)

  override def getHeader = header

  override def getSource = header.getSource

  override def getDestination = header.getDestination

  override def getProtocol = header.getProtocol

}
