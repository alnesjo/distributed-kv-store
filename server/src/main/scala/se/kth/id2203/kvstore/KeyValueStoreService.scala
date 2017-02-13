package se.kth.id2203.kvstore

import se.kth.id2203.link.NetworkMessage
import se.kth.id2203.overlay.Routing
import se.sics.kompics.network.{Address, Network, Transport}
import se.sics.kompics.sl._

class KeyValueStoreService extends ComponentDefinition {
  val net = requires[Network]
  val route = requires(Routing)

  val self = config.getValue("id2203.project.address", classOf[Address])

  net uponEvent {
    case NetworkMessage(source, _, _, Get(key)) => handle {
      trigger(NetworkMessage(self, source, Transport.TCP, Post(key, "Not implemented!")), net)
    }
    case NetworkMessage(source, _, _, Store(key, value)) => handle {
      trigger(NetworkMessage(self, source, Transport.TCP, Post(key, "Not implemented!")), net)
    }
  }

}
