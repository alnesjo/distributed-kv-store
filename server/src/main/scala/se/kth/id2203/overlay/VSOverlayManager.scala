package se.kth.id2203.overlay

import se.kth.id2203.bootstrapping.{Booted, Bootstrapping, GetInitialAssignments, InitialAssignments}
import se.kth.id2203.link.NetworkMessage
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.util.Random

class VSOverlayManager extends ComponentDefinition {

  val route = provides(Routing)
  val boot = requires(Bootstrapping)
  val net = requires(Network)
  val timer = requires(Timer)

  val self = config.getValue("id2203.project.address", classOf[Address])
  val rnd = new Random
  var lookupTable = new LookupTable()

  boot uponEvent {
    case GetInitialAssignments(nodes: Set[Address]) => handle {
      trigger(InitialAssignments(LookupTable.generate(nodes)), boot)
    }
    case Booted(lut: LookupTable) => handle {
      lookupTable = lut
    }
  }

  route uponEvent {
    case RouteMessage(key, NetworkMessage(`self`, _, _, payload)) => handle {
      val partition = lookupTable.lookup(key)
      val dst = partition.toVector(rnd.nextInt(partition.size))
      trigger(NetworkMessage(self, dst, Transport.TCP, payload), net)
    }
    case RouteMessage(key, NetworkMessage(other, _, _, payload)) => handle {
      val partition = lookupTable.lookup(key)
      val dst = partition.toVector(rnd.nextInt(partition.size))
      trigger(NetworkMessage(other, dst, Transport.TCP, payload), net)
    }
  }

  net uponEvent {
    case NetworkMessage(src, _, _, Connect(id)) => handle {
      val size = lookupTable.getNodes.size
      trigger(NetworkMessage(self, src, Transport.TCP, Ack(id, size)), net)
    }
  }


}
