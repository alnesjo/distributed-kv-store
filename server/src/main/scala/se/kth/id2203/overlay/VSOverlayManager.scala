package se.kth.id2203.overlay

import org.slf4j.LoggerFactory
import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.kth.id2203.link.NetworkMessage
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.util.Random

class VSOverlayManager extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[VSOverlayManager])
  val rnd = new Random

  val route = provides(Routing)
  val boot = requires(Bootstrapping)
  val net = requires[Network]
  val timer = requires[Timer]

  val self = cfg.getValue[Address]("id2203.project.address")
  var lookupTable: LookupTable = _

  boot uponEvent {
    case GetInitialAssignments(nodes: Set[Address]) => handle {
      log.info("Generating LookupTable...")
      val lut = LookupTable.generate(nodes)
      log.debug("Generated assignments:\n{}", lut)
      trigger(InitialAssignments(lut), boot)
    }
    case Booted(assignment) => handle {
      assignment match {
        case lut: LookupTable => {
          log.info("Got NodeAssignment, overlay ready.")
          lookupTable = lut
        }
        case _ => {
          log.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", assignment.getClass)
        }
      }
    }
  }

  route uponEvent {
    case RouteMessage(key, message) => handle {
      val partition = lookupTable.lookup(key)
      val dst = partition.toVector(rnd.nextInt(partition.size))
      log.info("Routing message for key {} to {}", key, dst: Any)
      trigger(NetworkMessage(self, dst, Transport.TCP, message), net)
    }
  }

  net uponEvent {
    case NetworkMessage(src, _, _, Connect(id)) => handle {
      if (null != lookupTable) {
        log.debug("Accepting connection request from {}", src)
        val size = lookupTable.getNodes.size
        trigger(NetworkMessage(self, src, Transport.TCP, Ack(id, size)), net)
      } else {
        log.info("Rejecting connection request from {}, as system is not ready, yet.", src)
      }
    }
  }


}
