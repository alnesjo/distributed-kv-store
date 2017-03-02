package se.kth.id2203

import se.kth.id2203.overlay.Identifier
import se.sics.kompics.KompicsEvent

package object kvstore {

  sealed trait Code
  case object Ok extends Code
  case object Error extends Code
  case object NotImplemented extends Code

  trait Operation extends KompicsEvent {
    val id: Identifier
  }

  trait Invocation extends Operation {
    val key: String
  }

  trait Response extends Operation {
    val status: Code
  }

  case class Error(id: Identifier) extends Response {
    override val status: Code = Error
  }

  case class NotImplemented(id: Identifier) extends Response {
    override val status: Code = NotImplemented
  }

}
