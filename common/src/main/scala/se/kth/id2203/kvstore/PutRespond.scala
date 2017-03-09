package se.kth.id2203.kvstore

case class PutRespond(id: Identifier, status: Code = Ok) extends Response
