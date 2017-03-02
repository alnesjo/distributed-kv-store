package se.kth.id2203.kvstore

trait Response extends Operation {
  val status: Code
}