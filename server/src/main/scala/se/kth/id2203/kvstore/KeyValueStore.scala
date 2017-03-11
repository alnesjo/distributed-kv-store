package se.kth.id2203.kvstore

import se.sics.kompics.sl.Port

class KeyValueStore extends Port {
  request[Invocation]
  indication[Response]
}
