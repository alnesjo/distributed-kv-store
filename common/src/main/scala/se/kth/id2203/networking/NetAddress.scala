package se.kth.id2203.networking

import java.net.{InetAddress, InetSocketAddress}
import se.sics.kompics.network.Address

case class NetAddress(address: InetAddress, port: Int) extends Address with Serializable with Comparable[NetAddress] {

  val isa = new InetSocketAddress(address, port)

  override def getIp = isa.getAddress

  override def sameHostAs(other: Address) = isa equals other.asSocket

  override def getPort = isa.getPort

  override def asSocket = isa

  override def compareTo(t: NetAddress) =
    10*(isa.getAddress.toString compareTo t.isa.getAddress.toString) + (getPort compareTo t.getPort)

}
