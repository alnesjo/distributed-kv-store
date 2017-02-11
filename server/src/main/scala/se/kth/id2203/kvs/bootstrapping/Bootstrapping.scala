package se.kth.id2203.kvs.bootstrapping

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl._

class Bootstrapping extends Port {

  indication[GetInitialAssignments]
  indication[Booted]
  request[InitialAssignments]

}

case class GetInitialAssignments(nodes: Set[Address]) extends KompicsEvent

case class Booted(assignment: NodeAssignment) extends KompicsEvent

case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

class NodeAssignment extends Serializable {}