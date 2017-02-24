package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.sics.kompics.network.Address
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.util.Random

object VSOverlayManager {

  case class Init(self: Address, replicationDegree: Int) extends se.sics.kompics.Init[VSOverlayManager]

}

class VSOverlayManager(init: VSOverlayManager.Init) extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[VSOverlayManager])
  val rnd = new Random

  val route = provides(Routing)
  val boot = requires(Bootstrapping)
  val pl = requires(PerfectLink)
  val timer = requires[Timer]

  val self = init.self
  val replicationDegree = init.replicationDegree

  var lookupTable: LookupTable = _

  boot uponEvent {
    case GetInitialAssignments(nodes: Set[Address]) => handle {
      log.info("Generating LookupTable...")
      val lut = LookupTable.generate(nodes, replicationDegree)
      log.debug(s"Generated assignments: $lut")
      trigger(InitialAssignments(lut) -> boot)
    }
    case Booted(assignment) => handle {
      assignment match {
        case lut: LookupTable =>
          log.info("Got NodeAssignment, overlay ready.")
          lookupTable = lut
        case _ =>
          log.error(s"Got invalid NodeAssignment type. Expected: LookupTable; Got: ${assignment.getClass}")
      }
    }
  }

  route uponEvent {
    case RouteMessage(key, message) => handle {
      val partition = lookupTable.lookup(key)
      val dst = partition.toVector(rnd.nextInt(partition.size))
      log.info(s"Routing message for key $key to $dst")
      trigger(PL_Send(dst, message) -> pl)
    }
  }

  pl uponEvent {
    case PL_Deliver(src, Connect(id)) => handle {
      if (null != lookupTable) {
        log.debug("Accepting connection request from {}", src)
        val size = lookupTable.getNodes.size
        trigger(PL_Send(src, Ack(id, size)) -> pl)
      } else {
        log.info("Rejecting connection request from {}, as system is not ready, yet.", src)
      }
    }
  }

}
