package se.kth.id2203.networking

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Msg, Transport}

case class NetMessage(src: NetAddress, dst: NetAddress, payload: KompicsEvent) extends Msg[NetAddress, NetHeader] with Serializable {
  val header = NetHeader(src, dst, Transport.TCP)
  override def getHeader = header
  override def getSource = header.getSource
  override def getDestination = header.getDestination
  override def getProtocol = header.getProtocol
}
