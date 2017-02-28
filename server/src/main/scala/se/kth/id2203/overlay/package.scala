package se.kth.id2203

import se.sics.kompics.sl.Port

package object overlay {

  object Routing extends Port {
    request[RouteMessage]
  }

}
