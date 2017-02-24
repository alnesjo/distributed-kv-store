package se.kth.id2203.link

import com.google.common.base.Optional
import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory
import se.sics.kompics.network.netty.serialization.{Serializer, Serializers}
import scala.pickling.Defaults._
import scala.pickling.json._

class PicklingSerializer extends Serializer {

  val log = LoggerFactory.getLogger(classOf[PicklingSerializer])

  override def identifier() = 200

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    log.trace(s"Serializing $o.")
    val json = o.pickle.value
    log.trace(s"Intermediate JSON $json.")
    Serializers.toBinary(json.getBytes, buf)
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val absent: Optional[AnyRef] = Optional.absent()
    val json = new String(Serializers.fromBinary(buf, absent).asInstanceOf[Array[Byte]])
    log.trace(s"Intermediate JSON $json.")
    val o = json.unpickle[AnyRef]
    log.trace(s"Deserialized $o.")
    o
  }

}
