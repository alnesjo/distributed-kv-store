import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, KompicsEvent, Start, Init => JInit}
//import se.sics.kompics.network.{Network}

sealed trait KeyValueStoreEvent extends KompicsEvent
case class Get(key: String) extends KeyValueStoreEvent // TODO allow keys and values other than strings
case class Store(key: String, value: String) extends KeyValueStoreEvent
case class Post(key: String, value: String) extends KeyValueStoreEvent

// Borrowing some ideas from the Java code skeleton which is confusing for now
case class RouteMessage(key: String, message: KompicsEvent) extends KompicsEvent

object RoutePort extends Port {
  request[RouteMessage]
  indication[RouteMessage]
}

class KeyValueStore extends ComponentDefinition {
  val input = requires(RoutePort)
  val output = provides(RoutePort)

  input uponEvent {
    case RouteMessage("This key is probably unrelated to the storage", Get(key)) => handle {
      ???
    }
    case Get(key) => handle {
      // Something along the lines of
      //   if this node contains key, then post it's value back
      //   otherwise ask someone else
      //   perhaps get-requests are multicast to begin with?
      trigger(RouteMessage("key?", Post(key, "Some value fetched from storage")), output)
      ???
    }
    case Store(key, value) => handle {
      ???
    }
    case Post(key, value) => handle {
      ???
    }
  }
}
