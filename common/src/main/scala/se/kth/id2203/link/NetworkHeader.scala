package se.kth.id2203.link

import se.sics.kompics.network.Address
import se.sics.kompics.network.Header
import se.sics.kompics.network.Transport

class NetworkHeader(val src: Address, val dst: Address, val ptc: Transport) extends Header[Address] {

  override def getSource: Address = src

  override def getDestination: Address = dst

  override def getProtocol: Transport = ptc

  override def toString: String = s"$getClass($src,$dst,$ptc)"
}