package se.kth.id2203.kvstore

import se.sics.kompics.KompicsEvent

case class Partition(map: Map[String, Any]) extends KompicsEvent with Map[String, String] {

  override def +[B1 >: String](kv: (String, B1)): Partition = Partition(map + kv)

  override def get(key: String): Option[String] = map.get(key).map(_.toString)

  override def iterator: Iterator[(String, String)] = map.mapValues(_.toString).map(identity).iterator

  override def -(key: String): Partition = Partition(map - key)
}
