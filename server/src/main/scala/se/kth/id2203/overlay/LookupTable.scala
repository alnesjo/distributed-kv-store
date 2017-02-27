package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.kth.id2203.bootstrapping.NodeAssignment
import se.kth.id2203.link.NetworkAddress
import se.sics.kompics.config.Conversions
import se.sics.kompics.network.Address

object LookupTable {
  def generate(nodes: Set[Address], replicationDegree: Int): LookupTable = {

    val log = LoggerFactory.getLogger(classOf[LookupTable])

    val lut = new LookupTable
    val (k, n) = nodes
      .map(a => s"${a.getIp.getHostAddress}:${a.getPort.toString}")
      .map(a => (a##, a))
      .toList
      .unzip
    log.trace(s"($k,$n)")
    lut.partitions = (0 until replicationDegree)
      .map(i => k zip ((n drop i) ++ (n take i)))
      .reduce(_ ++ _)
      .groupBy(_._1)
      .mapValues(l => l map (t => t._2) toSet)
    log.trace(s"${lut.partitions}")
    lut
  }
}

class LookupTable extends NodeAssignment {

  var partitions: Map[Int, Set[String]] = Map[Int, Set[String]]()

  def lookup(key: String): Set[Address] = partitions.get(key##) match {
    case Some(set) =>
      set.map(Conversions.convert(_,classOf[NetworkAddress]))
    case None =>
      partitions.values.last.map(Conversions.convert(_,classOf[NetworkAddress]))
  }

  def getNodes: Set[Address] = partitions.values.reduce(_ ++ _).map(Conversions.convert(_,classOf[NetworkAddress]))

  override def toString: String = s"$getClass($partitions)"

}
