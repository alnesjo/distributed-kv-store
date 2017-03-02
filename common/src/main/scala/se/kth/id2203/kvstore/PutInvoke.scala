package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier

case class PutInvoke(id: Identifier, key: String, value: String) extends Invocation
