package se.kth.id2203.register

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port

case class PAR_Read_Invoke(partition: Integer) extends KompicsEvent
case class PAR_Read_Respond(partition: Integer, value: Option[Any]) extends KompicsEvent
case class PAR_Write_Invoke(partition: Integer, value: Any) extends KompicsEvent
case class PAR_Write_Respond(partition: Integer) extends KompicsEvent

class PartitionedAtomicRegister extends Port {
  request[PAR_Read_Invoke]
  request[PAR_Write_Invoke]
  indication[PAR_Read_Respond]
  indication[PAR_Write_Respond]
}