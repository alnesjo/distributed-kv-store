package se.kth.id2203.overlay

import se.kth.id2203.bootstrapping.NodeAssignment
import se.sics.kompics.network.Address
import scala.collection.mutable

object LookupTable {
  def generate(nodes: Set[Address]): LookupTable = {
    val lut = new LookupTable
    val d = 3 // Replication degree
    val (k, n) = nodes.map(a => (a.hashCode(), a)).toList.unzip
    for ((x, y) <- (0 to d).map(i => k zip ((n drop i) ++ (n take i))).reduce(_ ++ _)) {
      lut.partitions addBinding (x, y)
    }
    lut
  }
}

class LookupTable extends NodeAssignment {

  private val partitions = new mutable.HashMap[Int, mutable.Set[Address]] with mutable.MultiMap[Int, Address]

  def lookup(key: Int) = partitions.get(key) match {
    case Some(_) => partitions(key)
    case None => partitions.values.last
  }

  def getNodes = partitions.values.reduce(_ ++ _)

  override def toString = ???

}
