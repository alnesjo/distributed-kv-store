package se.kth.id2203

import com.google.common.base.Optional
import com.sun.jndi.cosnaming.IiopUrl.Address
import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory
import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.network.netty.serialization.{Serializer, Serializers}

import scala.pickling.Defaults._
import scala.pickling.json._

class SpecialSerializer extends Serializer {

  val log = LoggerFactory.getLogger(classOf[SpecialSerializer])

  val LOOKUP_TABLE = 1

  override def identifier() = 210

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    o match {
      case o: LookupTable =>
        buf.writeByte(LOOKUP_TABLE)
        log.trace(s"Serializing $o.")
        val json = o.pickle.value
        log.trace(s"Intermediate JSON $json.")
        Serializers.toBinary(json.getBytes, buf)
    }

  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    buf.readByte match {
      case LOOKUP_TABLE =>
        val absent: Optional[AnyRef] = Optional.absent()
        val json = new String(Serializers.fromBinary(buf, absent).asInstanceOf[Array[Byte]])
        log.trace(s"Intermediate JSON $json.")
        val o = json.unpickle[LookupTable]
        log.trace(s"Deserialized $o.")
        o
    }
  }

}
