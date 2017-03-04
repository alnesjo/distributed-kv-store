package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.sics.kompics.network.Address

object LookupTable {

  def generate(nodes: Set[Address], rpd: Int): LookupTable = {
    val (k, n) = nodes
      .map(a => (a.##, a))
      .toList
      .unzip
    val partitions = (0 until rpd)
      .map(i => k zip ((n drop i) ++ (n take i)))
      .reduce(_ ++ _)
      .groupBy(_._1)
      .mapValues(_ map (_._2))
      .mapValues(_ toSet)
      .map(identity) // https://issues.scala-lang.org/browse/SI-7005
    LookupTable(partitions, Set.empty)
  }

}

case class LookupTable(partitions: Map[Int, Set[Address]], exclude: Set[Address]) {

  val log = LoggerFactory.getLogger(classOf[LookupTable])

  def lookup(key: Any): Set[Address] = partitions.keys.filter(_ <= key.##).lastOption match {
    case Some(partition) => partitions(partition).filterNot(exclude)
    case None => partitions.values.last.filterNot(exclude)
  }

  def getNodes: Set[Address] = partitions.values.reduce(_ ++ _).filterNot(exclude)

  def -(address: Address): LookupTable = LookupTable(partitions, exclude + address)

  def +(address: Address): LookupTable = LookupTable(partitions, exclude - address)

}
