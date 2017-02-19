package se.kth.id2203.link

import java.net.{InetAddress, InetSocketAddress}

import se.sics.kompics.network.Address

class NetworkAddress(address: InetAddress, port: Int) extends Address with Comparable[NetworkAddress] {

  val isa = new InetSocketAddress(address, port)

  override def getIp = isa.getAddress

  override def sameHostAs(other: Address) = asSocket equals other.asSocket

  override def getPort = isa.getPort

  override def asSocket = isa

  override def compareTo(t: NetworkAddress) = isa.getAddress.toString compareTo t.isa.getAddress.toString match {
    case 0 => getPort compareTo t.getPort
    case b => b
  }

  override def toString: String = s"$getIp:$getPort"

}