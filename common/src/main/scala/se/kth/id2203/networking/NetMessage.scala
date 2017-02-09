package se.kth.id2203.networking

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Address, Msg, Transport}

case class NetMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
  extends Msg[NetAddress, NetHeader] with Serializable {

  val header = NetHeader(src, dst, ptc)

  override def getHeader = header

  override def getSource = header.getSource

  override def getDestination = header.getDestination

  override def getProtocol = header.getProtocol

}
