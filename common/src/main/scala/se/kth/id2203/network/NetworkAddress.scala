package se.kth.id2203.network

import java.net.{InetAddress, InetSocketAddress}
import se.sics.kompics.network.Address

case class NetworkAddress(address: InetAddress, port: Int) extends Address with Serializable with Comparable[NetworkAddress] {

  val isa = new InetSocketAddress(address, port)

  override def getIp = isa.getAddress

  override def sameHostAs(other: Address) = isa equals other.asSocket

  override def getPort = isa.getPort

  override def asSocket = isa

  override def compareTo(t: NetworkAddress) =
    10*(isa.getAddress.toString compareTo t.isa.getAddress.toString) + (getPort compareTo t.getPort)

}