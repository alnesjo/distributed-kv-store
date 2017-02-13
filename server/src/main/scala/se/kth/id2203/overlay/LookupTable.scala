package se.kth.id2203.overlay

import se.kth.id2203.bootstrapping.NodeAssignment
import se.sics.kompics.network.Address
import scala.collection.mutable

object LookupTable {
  def generate(nodes: Set[Address]): LookupTable = {
    val lut = new LookupTable
    lut.partitions.put("init", nodes)
    return lut
  }
}

class LookupTable extends NodeAssignment {

  val partitions = new mutable.HashMap[String, Set[Address]] with mutable.MultiMap[String, Address]

  def lookup(key: String) = partitions.get(key) match {
    case Some(_) => partitions(key)
    case None => partitions.values.last
  }

  def getNodes = partitions.values

  override def toString = ???

}