package se.kth.id2203.networking

import se.sics.kompics.network.{Address, Header, Transport}

case class NetHeader(src: Address, dst: Address, ptc: Address) extends Header[NetAddress] {

  override def getSource = src

  override def getDestination = dst

  override def getProtocol = ptc

}
