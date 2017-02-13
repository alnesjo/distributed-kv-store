package se.kth.id2203

import se.sics.kompics.KompicsEvent

package object kvstore {

  sealed trait Operation extends KompicsEvent

  case class Get(key: String) extends Operation

  case class Store(key: String, value: String) extends Operation

  case class Post(key: String, value: String) extends Operation

  // TODO keys/values other than strings?

}
