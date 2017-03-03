package se.kth.id2203.kvstore

import se.kth.id2203.overlay.Identifier

case class GetRespond(id: Identifier, value: Option[String], status: Code = Ok) extends Response