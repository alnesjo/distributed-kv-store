package se.kth.id2203.kvstore

case class GetRespond(id: Identifier, value: Option[String], status: Code = Ok) extends Response