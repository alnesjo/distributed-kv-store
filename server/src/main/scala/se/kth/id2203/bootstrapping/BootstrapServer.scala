package se.kth.id2203.bootstrapping

import java.util.UUID
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.network.Network
import se.sics.kompics.timer.{SchedulePeriodicTimeout, CancelPeriodicTimeout, Timer, Timeout}
import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, Start, Init => JInit}

sealed trait State
case object Collecting extends State
case object Seeding extends State
case object Done extends State

class BSTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

class BootstrapServer extends ComponentDefinition {
  val boot = provides(Bootstrapping)
  val net = requires[Network]
  val timer = requires[Timer]

  val self = config getValue("id2203.project.address", classOf[NetAddress])
  val bootThreshold = config getValue("id2203.project.bootThreshold", classOf[Int])

  var state = Collecting
  var timeoutId = UUID
  var active = scala.collection.mutable.Set[NetAddress]()
  var ready = scala.collection.mutable.Set[NetAddress]()
  var initialAssignment = None

  ctrl uponEvent {
    case _: Start => handle {
      val timeout = 2 * (config getValue("id2203.project.keepAlivePeriod", classOf[Long]))
      var spt = new SchedulePeriodicTimeout(timeout, timeout)
      spt.setTimeoutEvent(new BSTimeout(spt))
      trigger(spt, timer)
      timeoutId = spt.getTimeoutEvent.getTimeoutId
      active add self
    }
  }
}
