package se.kth.id2203.kvs

import se.sics.kompics.KompicsEvent

sealed trait Operation extends KompicsEvent

case class Get(key: String) extends Operation

case class Store(key: String, value: String) extends Operation

case class Post(key: String, value: String) extends Operation

// TODO keys/values other than strings?