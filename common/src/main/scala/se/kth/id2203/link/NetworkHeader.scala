package se.kth.id2203.link

import se.sics.kompics.network.{Address, Header, Transport}

case class NetworkHeader(src: Address, dst: Address, ptc: Transport) extends Header[Address] {

  override def getSource = src

  override def getDestination = dst

  override def getProtocol = ptc

}
