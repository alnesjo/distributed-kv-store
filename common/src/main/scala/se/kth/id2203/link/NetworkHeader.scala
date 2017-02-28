package se.kth.id2203.link

import se.sics.kompics.network.Address
import se.sics.kompics.network.Header
import se.sics.kompics.network.Transport

class NetworkHeader(val src: Address, val dst: Address, val ptc: Transport) extends Header[Address] {

  override def getSource: Address = this.src

  override def getDestination: Address = this.dst

  override def getProtocol: Transport = this.ptc

}