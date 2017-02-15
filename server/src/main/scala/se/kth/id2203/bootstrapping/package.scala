package se.kth.id2203

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timeout}

package object bootstrapping {

  class NodeAssignment extends Serializable

  case class GetInitialAssignments(nodes: Set[Address]) extends KompicsEvent

  case class Booted(assignment: NodeAssignment) extends KompicsEvent

  case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

  object Bootstrapping extends Port {
    indication[GetInitialAssignments]
    indication[Booted]
    request[InitialAssignments]
  }

  class BootstrapTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

  case class Boot(assignment: NodeAssignment) extends KompicsEvent with Serializable

  case object Active extends KompicsEvent

  case object Ready extends KompicsEvent

}
