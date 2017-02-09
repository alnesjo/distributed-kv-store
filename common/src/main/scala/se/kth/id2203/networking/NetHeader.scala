package se.kth.id2203.networking

import se.sics.kompics.network.{Header, Transport}

/**
  * Created by robert on 2017-02-09.
  */
case class NetHeader(val src: NetAddress, val dst: NetAddress, val ptc: Transport) extends Header[NetAddress] {
  override def getSource = src
  override def getDestination = dst
  override def getProtocol = ptc
}
