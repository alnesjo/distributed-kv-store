package se.kth.id2203.bootstrapping

import java.util.UUID

import se.kth.id2203.link.{Deliver, PerfectLink, Send}
import se.sics.kompics.network.Address
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.kompics.sl._
import se.sics.kompics.{KompicsEvent, Start}

class BootstrapServer extends ComponentDefinition {

  val boot = provides[Bootstrapping]
  val pl = requires[PerfectLink]
  val timer = requires[Timer]
  // Should probably be able to use some reliable broadcasting abstraction rather than a raw link

  val self = config getValue("id2203.project.address", classOf[Address])
  val bootThreshold = config getValue("id2203.project.bootThreshold", classOf[Int])

  var state: State = Collecting
  var timeoutId: UUID = ??? // What is the point of this?
  var active = Set[Address]()
  var ready = Set[Address]()
  var initialAssignment: NodeAssignment = ??? // Do we have a notion of an empty assignment?

  ctrl uponEvent {
    case _: Start => handle {
      val timeout = 2 * (config getValue("id2203.project.keepAlivePeriod", classOf[Long]))
      var spt = new SchedulePeriodicTimeout(timeout, timeout)
      spt.setTimeoutEvent(new BootstrapTimeout(spt))
      trigger(spt, timer)
      timeoutId = spt.getTimeoutEvent.getTimeoutId
      active += self
    }
  }

  timer uponEvent {
    case _: BootstrapTimeout => handle {
      state match {
        case Collecting =>
          if (active.size >= bootThreshold) {
            state = Seeding
            trigger(GetInitialAssignments(active), boot)
          }
        case Seeding =>
          if (ready.size >= bootThreshold) {
            trigger(Booted(initialAssignment), boot)
            state = Done
          }
        case Done =>
          trigger(CancelPeriodicTimeout, timer)
          suicide()
      }
    }
  }

  boot uponEvent {
    case InitialAssignments(assignment) => handle {
      initialAssignment = assignment
      for (node <- active) {
        trigger(Send(node, Boot(initialAssignment)), pl)
      }
      ready += self
    }
  }

  pl uponEvent {
    case Deliver(src, Active) => handle {
      active += src
    }
    case Deliver(src, Ready) => handle {
      ready += src
    }
  }

}

class BootstrapTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

case class Boot(assignment: NodeAssignment) extends KompicsEvent with Serializable

case object Active extends KompicsEvent
case object Ready extends KompicsEvent
