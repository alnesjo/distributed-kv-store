package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier

case class PutRespond(id: Identifier) extends Response {
  override val status: Code = Ok
}
