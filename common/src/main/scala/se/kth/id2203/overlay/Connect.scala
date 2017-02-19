package se.kth.id2203.overlay

import java.util.UUID

import se.sics.kompics.KompicsEvent

object Connect {

  case class Ack(id: UUID, clusterSize: Int) extends KompicsEvent with Serializable

}

case class Connect(id: UUID) extends KompicsEvent with Serializable {

  def ack(clusterSize: Int) = Connect.Ack(id, clusterSize)

}