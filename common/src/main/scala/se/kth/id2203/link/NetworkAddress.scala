package se.kth.id2203.link

import com.google.common.collect.ComparisonChain
import com.google.common.primitives.UnsignedBytes
import java.net.{InetAddress, InetSocketAddress}

import se.sics.kompics.network.Address

class NetworkAddress(val isa: InetSocketAddress) extends Address with Comparable[NetworkAddress] {

  def getIp: InetAddress = this.isa.getAddress

  def getPort: Int = this.isa.getPort

  def asSocket: InetSocketAddress = this.isa

  def sameHostAs(other: Address): Boolean = this.asSocket equals other.asSocket

  override final def toString: String = s"${this.isa.getAddress.getHostAddress}:${this.isa.getPort.toString}"

  override def equals(obj: Any): Boolean = obj match {
    case other: NetworkAddress =>
      0 == this.compareTo(other)
    case _ => false
  }

  def compareTo(that: NetworkAddress): Int = ComparisonChain
    .start
    .compare(this.getIp.getAddress, that.getIp.getAddress, UnsignedBytes.lexicographicalComparator)
    .compare(this.getPort, that.getPort)
    .result

}