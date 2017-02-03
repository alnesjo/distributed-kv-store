import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, KompicsEvent, Start, Init => JInit}

sealed trait KeyValueStoreEvent extends KompicsEvent
case class Get(key: Int) extends KeyValueStoreEvent // TODO allow keys and values other than itegers and strings
case class Store(key: Int, value: String) extends KeyValueStoreEvent
case class Post(key: Int, value: String) extends KeyValueStoreEvent

object KeyValueStorePort extends Port {
  request[KeyValueStoreEvent]
  indication[KeyValueStoreEvent]
}

/**
  * Key-value storage
  * Ring structure?
  */
class KeyValueStoreComponent extends ComponentDefinition {
  // Predecessor ports
  val predIn = requires(KeyValueStorePort)
  val predOut = provides(KeyValueStorePort)
  // Successor ports
  val succIn = requires(KeyValueStorePort)
  val succOut = provides(KeyValueStorePort)
  // Client ports
  val clientIn = requires(KeyValueStorePort)
  val clientOut = provides(KeyValueStorePort)

  clientIn uponEvent {
    case Get(key) => handle {
      // Something along the lines of
      //   if this node contains key, then post it's value back
      //   otherwise ask someone else
      trigger(Post(key, "Some value fetched from storage"), clientOut)
      trigger(Get(key), succOut)
      ???
    }
    case Store(key, value) => handle {
      ???
    }
    case Post(key, value) => handle {
      // Clients should not post, right?
      ???
    }
  }
  predIn uponEvent {???}
  succIn uponEvent {???}
}
