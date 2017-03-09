package se.kth.id2203.kvstore

case class GetInvoke(id: Identifier, key: String) extends Invocation
