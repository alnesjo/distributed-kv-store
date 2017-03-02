package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier

case class GetInvoke(id: Identifier, key: String) extends Invocation
