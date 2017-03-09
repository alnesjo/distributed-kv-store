package se.kth.id2203.kvstore

case class PutInvoke(id: Identifier, key: String, value: String) extends Invocation