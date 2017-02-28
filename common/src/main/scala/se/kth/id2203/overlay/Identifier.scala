package se.kth.id2203.overlay

import java.util.UUID

import se.sics.kompics.network.Address

case object Identifier {

  /**
    * New unique identifier from source address
    * @param src Source address
    * @return Identifier(UUID.randomUUID, src)
    */
  def fromSource(src: Address) = Identifier(UUID.randomUUID, src)

}

case class Identifier(uuid: UUID, src: Address) extends Comparable[Identifier] {

  override def compareTo(that: Identifier): Int = this.uuid compareTo that.uuid

}
