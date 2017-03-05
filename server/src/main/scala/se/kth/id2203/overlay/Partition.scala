package se.kth.id2203.overlay

import se.sics.kompics.KompicsEvent

case class Partition(map: Map[String, String]) extends KompicsEvent with Map[String, String] {

  override def +[B1 >: String](kv: (String, B1)): Map[String, B1] = map + kv

  override def get(key: String): Option[String] = map.get(key)

  override def iterator: Iterator[(String, String)] = map.iterator

  override def -(key: String): Map[String, String] = map - key

}
