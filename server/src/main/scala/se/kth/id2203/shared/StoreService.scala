package se.kth.id2203.kvstore

import org.slf4j.LoggerFactory
import se.kth.id2203.link.NetworkMessage
import se.kth.id2203.overlay.Routing
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

class StoreService extends ComponentDefinition {

  val log = LoggerFactory.getLogger(classOf[StoreService])

  val net = requires[Network]
  val route = requires(Routing)

  val self = cfg.getValue[Address]("id2203.project.address")

  net uponEvent {
    case NetworkMessage(source, _, _, Get(key)) => handle {
      trigger(NetworkMessage(self, source, Transport.TCP, Post(key, "Not implemented!")), net)
    }
    case NetworkMessage(source, _, _, Store(key, value)) => handle {
      trigger(NetworkMessage(self, source, Transport.TCP, Post(key, "Not implemented!")), net)
    }
  }

}
