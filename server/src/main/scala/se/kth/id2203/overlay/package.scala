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

  case class RouteMessage(key: String, message: KompicsEvent) extends KompicsEvent

  case class Connect(id: UUID) extends KompicsEvent with Serializable

  case class Ack(id: UUID, clusterSize: Int) extends KompicsEvent with Serializable

}
