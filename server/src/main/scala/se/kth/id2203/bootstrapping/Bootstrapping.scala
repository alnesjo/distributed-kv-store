package se.kth.id2203.bootstrapping

import se.kth.id2203.networking._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._

class NodeAssignment extends Serializable {}

case class GetInitialAssignments(nodes: Set[NetAddress]) extends KompicsEvent
case class Booted(assignment: NodeAssignment) extends KompicsEvent
case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

object Bootstrapping extends Port {
  indication[GetInitialAssignments]
  indication[Booted]
  request[InitialAssignments]
}
