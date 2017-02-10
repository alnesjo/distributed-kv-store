package se.kth.id2203.network

import se.sics.kompics.network.{Address, Header, Transport}

case class NetworkHeader(src: Address, dst: Address, ptc: Address) extends Header[NetworkAddress] {

  override def getSource = src

  override def getDestination = dst

  override def getProtocol = ptc

}
