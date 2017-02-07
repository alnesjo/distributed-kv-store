import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, KompicsEvent, Start, Init => JInit}
import se.sics.kompics.network.{Network}

sealed trait KeyValueStoreEvent extends KompicsEvent
case class Get(key: String) extends KeyValueStoreEvent // TODO allow keys and values other than strings
case class Store(key: String, value: String) extends KeyValueStoreEvent
case class Post(key: String, value: String) extends KeyValueStoreEvent

class KeyValueStore extends ComponentDefinition {
  val input = requires[Network]
  val output = provides[Network]
  ctrl uponEvent {
    case _: Start => handle {
      println("Requesting value at A")
      trigger(Get("A"), output)
    }
  }
  input uponEvent {
    case Get("A") => handle {
      trigger(Post("A", "apple"), output)
    }
    case Get("next key") => handle {
    }
    case Get(key) => handle {
      trigger(Post(key, "some fruit"), output)
    }
    case Store(key, value) => handle {
    }
    case Post("A", value) => handle {
      println("Requesting value at B")
      trigger(Get("B"), output)
    }
    case Post(key, value) => handle {
      println("Requesting value at next key")
      trigger(Get("next key"), output)
    }
  }
}

class KeyValueStoreService extends ComponentDefinition {
  val boot = create(classOf[KeyValueStore], JInit.NONE)
  connect[Network](boot -> boot)
}

object Main2 extends App {
  Kompics.createAndStart(classOf[KeyValueStoreService])
  Kompics.waitForTermination()
}
