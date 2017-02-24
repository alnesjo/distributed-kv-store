package se.kth.id2203

import java.util.UUID

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port

/**
  * Created by robert on 2017-02-13.
  */
package object overlay {

  object Routing extends Port {
    request[RouteMessage]
  }

}
