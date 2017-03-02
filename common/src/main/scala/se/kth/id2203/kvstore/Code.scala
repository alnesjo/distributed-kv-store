package se.kth.id2203.kvstore

sealed trait Code
case object Ok extends Code
case object Error extends Code