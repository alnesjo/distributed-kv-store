package se.kth.id2203.bootstrapping

import se.kth.id2203.kvstore._
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.{Kompics, Start, Init => JInit}

class KeyValueStoreDemo extends ComponentDefinition {
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
      Kompics.asyncShutdown()
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

class KeyValueStoreDemoService extends ComponentDefinition {
  val boot = create(classOf[KeyValueStoreDemo], JInit.NONE)
  connect[Network](boot -> boot)
}

object KeyValueStoreDemoMain extends App {
  Kompics.createAndStart(classOf[KeyValueStoreDemoService])
  Kompics.waitForTermination()
}
