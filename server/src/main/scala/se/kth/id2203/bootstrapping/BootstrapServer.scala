package se.kth.id2203.bootstrapping

import java.util.UUID

import se.kth.id2203.networking._
import se.sics.kompics.network.Network
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}
import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, KompicsEvent, Start, Init => JInit}

sealed trait State
case object Collecting extends State
case object Seeding extends State
case object Done extends State

class BootstrapTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

case class Boot(assignment: NodeAssignment) extends KompicsEvent with Serializable

case object Active extends KompicsEvent
case object Ready extends KompicsEvent

class BootstrapServer extends ComponentDefinition {
  val boot = provides(Bootstrapping)
  val net = requires[Network]
  val timer = requires[Timer]

  val self = config getValue("id2203.project.address", classOf[NetAddress])
  val bootThreshold = config getValue("id2203.project.bootThreshold", classOf[Int])

  var state: State = Collecting
  var timeoutId: UUID // What is the point of this?
  var active = Set[NetAddress]()
  var ready = Set[NetAddress]()
  var initialAssignment: NodeAssignment = ???

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
          suicide()
      }
    }
  }

  boot uponEvent {
    case InitialAssignments(assignment) => handle {
      initialAssignment = assignment
      for (node <- active) trigger(new NetMessage(self, node, Boot(initialAssignment)), net)
      ready += self
    }
  }

  net uponEvent {
    case msg: NetMessage => handle {
      // payload=Active?
      active += msg.getSource
    }
    case msg: NetMessage => handle {
      // payload=Ready?
      ready += msg.getSource
    }
  }
}
