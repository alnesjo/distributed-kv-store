package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.sics.kompics.network.Address

object LookupTable {

  def generate(nodes: Set[Address], replicationDegree: Int): LookupTable = {
    val (k, n) = nodes
      .map(a => (a.toString.##, a))
      .toList
      .unzip
    val partitions = (0 until replicationDegree)
      .map(i => k zip ((n drop i) ++ (n take i)))
      .reduce(_ ++ _)
      .groupBy(_._1)
      .mapValues(_ map (_._2))
      .mapValues(_ toSet)
      .map(identity) // https://issues.scala-lang.org/browse/SI-7005
    LookupTable(partitions)

  }

}

case class LookupTable(partitions: Map[Int, Set[Address]]) {

  val log = LoggerFactory.getLogger(classOf[LookupTable])

  def lookup(key: String): Set[Address] = partitions.keys.filter(_ <= key.##).lastOption match {
    case Some(partition) => partitions(partition)
    case None => partitions.values.last
  }

  def getNodes: Set[Address] = partitions.values.reduce(_ ++ _)

}
