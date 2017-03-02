package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier

case class NotImplemented(id: Identifier, status: Code = Error) extends Response
