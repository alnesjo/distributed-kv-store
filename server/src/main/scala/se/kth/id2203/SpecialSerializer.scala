package se.kth.id2203

import com.google.common.base.Optional
import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory
import se.kth.id2203.bootstrapping.Boot
import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.network.netty.serialization.{Serializer, Serializers}

import scala.pickling.Defaults._
import scala.pickling.json._

class SpecialSerializer extends Serializer {

  val log = LoggerFactory.getLogger(classOf[SpecialSerializer])

  val BOOT = 1
  val LOOKUP_TABLE = 2

  override def identifier() = 210

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    o match {
      case boot@Boot(lut: LookupTable) =>
        buf.writeByte(BOOT)
        log.trace(s"Serializing $boot.")
        Serializers.toBinary(lut, buf)
      case lut: LookupTable =>
        buf.writeByte(LOOKUP_TABLE)
        log.trace(s"Serializing $lut.")
        val json = lut.pickle.value
        log.trace(s"Intermediate JSON $json.")
        Serializers.toBinary(json.getBytes, buf)
    }

  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    buf.readByte match {
      case BOOT =>
        val absent: Optional[AnyRef] = Optional.absent()
        val lut = Serializers.fromBinary(buf, absent).asInstanceOf[LookupTable]
        val boot = Boot(lut)
        log.trace(s"Deserialized $boot.")
        boot
      case LOOKUP_TABLE =>
        val absent: Optional[AnyRef] = Optional.absent()
        val json = new String(Serializers.fromBinary(buf, absent).asInstanceOf[Array[Byte]])
        log.trace(s"Intermediate JSON $json.")
        val lut = json.unpickle[LookupTable]
        log.trace(s"Deserialized $lut.")
        lut
    }
  }

}
