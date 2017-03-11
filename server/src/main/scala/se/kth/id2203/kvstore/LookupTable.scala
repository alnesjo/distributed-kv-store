package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.sics.kompics.network.Address


object LookupTable {

  def generate(nodes: Set[Address], rpd: Int): LookupTable = {
    val (k, n) = nodes.map(a => a.## -> a).toList.unzip
    val partitions = (0 until rpd)
      .map(i => k zip ((n drop i) ++ (n take i)))
      .reduce(_ ++ _)
      .groupBy(_._1)
      .mapValues(_ map (_._2))
      .mapValues(_.toSet)
      .map(identity) // https://issues.scala-lang.org/browse/SI-7005
    LookupTable(partitions, Set.empty)
  }

}

/**
  * @param replicators Maps partition keys to replicator nodes.
  * @param excluded Nodes excluded from the system.
  */
case class LookupTable(replicators: Map[Int, Set[Address]], excluded: Set[Address]) {

  val log = LoggerFactory.getLogger(classOf[LookupTable])

  /** Key of the partition which {@code key} belongs to. */
  def lookup(key: Any): Int = replicators.keys.filter(_ <= key.##).lastOption match {
    case Some(partition) => partition
    case None => replicators.keys.last
  }

  /** All partitions that {@code node} replicates. */
  def partitions(node: Address): Set[Int] = replicators.filter(_._2 contains node).keySet

  /** All nodes in the system. */
  def nodes: Set[Address] = replicators.values.reduce(_ ++ _).filterNot(excluded)

  /** Add node {@code node} to exclusion filter. */
  def -(node: Address): LookupTable = LookupTable(replicators, excluded + node)

  /** Remove node {@code node} from exclusion filter. Does nothing if {@code node} was not already excluded. */
  def +(node: Address): LookupTable = LookupTable(replicators, excluded - node)

}
