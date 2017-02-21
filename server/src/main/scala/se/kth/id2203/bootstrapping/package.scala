package se.kth.id2203

import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port
import se.sics.kompics.timer.{SchedulePeriodicTimeout, Timeout}

package object bootstrapping {

  case class GetInitialAssignments(nodes: Set[Address]) extends KompicsEvent

  case class Booted(assignment: NodeAssignment) extends KompicsEvent

  case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

  object Bootstrapping extends Port {
    indication[GetInitialAssignments]
    indication[Booted]
    request[InitialAssignments]
  }

}
