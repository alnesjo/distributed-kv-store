package se.kth.id2203.bootstrapping

import se.kth.id2203.bootstrapping.Bootstrapping._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class NodeAssignment extends Serializable {}

object Bootstrapping {

  case class GetInitialAssignments(nodes: Set[Address]) extends KompicsEvent

  case class Booted(assignment: NodeAssignment) extends KompicsEvent

  case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

}

class Bootstrapping extends Port {

  indication[GetInitialAssignments]
  indication[Booted]
  request[InitialAssignments]

}
