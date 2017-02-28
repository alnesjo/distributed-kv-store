package se.kth.id2203.link

import com.google.common.base.Optional
import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory
import se.sics.kompics.network.netty.serialization.Serializer
import scala.pickling.shareNothing._
import scala.pickling.Defaults._
import scala.pickling.json._


class PicklingSerializer extends Serializer {

  implicit val addressPickler = NetworkAddressPickler
  scala.pickling.runtime.GlobalRegistry.picklerMap += (addressPickler.tag.key -> (x => addressPickler))
  scala.pickling.runtime.GlobalRegistry.unpicklerMap += (addressPickler.tag.key -> addressPickler)
  implicit val transportPickler = TransportPickler
  scala.pickling.runtime.GlobalRegistry.picklerMap += (transportPickler.tag.key -> (x => transportPickler))
  scala.pickling.runtime.GlobalRegistry.unpicklerMap += (transportPickler.tag.key -> transportPickler)

  val log = LoggerFactory.getLogger(classOf[PicklingSerializer])

  override def identifier() = 200

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    log.trace(s"Pickling: $o.")
    val json = o.pickle.value
    log.trace(s"Pickled JSON: $json.")
    val bytes = json.getBytes
    buf.writeInt(bytes.length)
    buf.writeBytes(bytes)
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val len = buf.readInt()
    val bytes = Array.ofDim[Byte](len)
    buf.readBytes(bytes)
    val json = new String(bytes)
    log.trace(s"Unpickled JSON: $json.")
    val o = json.unpickle[AnyRef]
    log.trace(s"Unpickled: $o.")
    o
  }

  // a nice implicit conversion between Guava's Optional and Scala's Option
  // in case anyone wants to call our serializer manually from Scala code
  implicit def optional2Option[T](o: Option[T]): Optional[T] = o match {
    case Some(x) => Optional.of(x)
    case None => Optional.absent()
  }

}