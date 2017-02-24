package se.kth.id2203.overlay

import se.kth.id2203.bootstrapping.NodeAssignment
import se.sics.kompics.network.Address
import scala.collection.mutable

object LookupTable {
  def generate(nodes: Set[Address], replicationDegree: Int): LookupTable = {
    val lut = new LookupTable
    val (k, n) = nodes.map(a => (a.hashCode(), a)).toList.unzip
    lut.partitions = (0 until replicationDegree)
      .map(i => k zip ((n drop i) ++ (n take i)))
      .reduce(_ ++ _)
      .groupBy(_._1)
      .mapValues(l => l map (t => t._2) toSet)
    lut
  }
}

class LookupTable extends NodeAssignment {

  private var partitions = Map[Int, Set[Address]]()

  def lookup(key: String) = partitions.get(key.hashCode) match {
    case Some(_) => partitions(key.hashCode)
    case None => partitions.values.last
  }

  def getNodes = partitions.values.reduce(_ ++ _)

  override def toString = s"$getClass($partitions)"

}
