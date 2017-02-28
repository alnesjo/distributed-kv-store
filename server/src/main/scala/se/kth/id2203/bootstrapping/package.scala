package se.kth.id2203

import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Address
import se.sics.kompics.sl.Port

package object bootstrapping {

  case class GetInitialAssignments(nodes: Set[Address]) extends KompicsEvent

  case class Booted(assignment: LookupTable) extends KompicsEvent

  case class InitialAssignments(assignment: LookupTable) extends KompicsEvent

  object Bootstrapping extends Port {
    indication[GetInitialAssignments]
    indication[Booted]
    request[InitialAssignments]
  }

}
