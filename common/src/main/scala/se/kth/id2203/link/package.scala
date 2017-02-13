package se.kth.id2203

import java.net.{InetAddress, InetSocketAddress}

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Address, Header, Msg, Transport}

package object link {

  case class NetworkMessage(src: Address, dst: Address, ptc: Transport, payload: KompicsEvent)
    extends Msg[Address, NetworkHeader] with Serializable {

    val header = NetworkHeader(src, dst, ptc)

    override def getHeader = header

    override def getSource = header.getSource

    override def getDestination = header.getDestination

    override def getProtocol = header.getProtocol

  }

  case class NetworkHeader(src: Address, dst: Address, ptc: Transport) extends Header[Address] {

    override def getSource = src

    override def getDestination = dst

    override def getProtocol = ptc

  }

  case class NetworkAddress(address: InetAddress, port: Int) extends Address with Serializable with Comparable[NetworkAddress] {

    val isa = new InetSocketAddress(address, port)

    override def getIp = isa.getAddress

    override def sameHostAs(other: Address) = isa equals other.asSocket

    override def getPort = isa.getPort

    override def asSocket = isa

    override def compareTo(t: NetworkAddress) =
      10*(isa.getAddress.toString compareTo t.isa.getAddress.toString) + (getPort compareTo t.getPort)

  }

}
