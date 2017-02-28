package se.kth.id2203.link

import se.sics.kompics.network.Transport

import scala.pickling._

object TransportPickler extends Pickler[Transport] with Unpickler[Transport] with pickler.PrimitivePicklers {

  override val tag = FastTypeTag[Transport]

  override def pickle(picklee: Transport, builder: PBuilder): Unit = {
    builder.hintTag(tag) // This is always required
    builder.beginEntry(picklee)
    builder.putField("ordinal", { fieldBuilder =>
      fieldBuilder.hintTag(bytePickler.tag)
      fieldBuilder.hintStaticallyElidedType()
      bytePickler.pickle(picklee.ordinal().toByte, fieldBuilder)
    })
    builder.endEntry()
  }

  override def unpickle(tag: String, reader: PReader): Any = {
    val ordinalReader = reader.readField("ordinal")
    ordinalReader.hintStaticallyElidedType()
    val ordinal = bytePickler.unpickleEntry(ordinalReader).asInstanceOf[Byte].toInt
    Transport.values()(ordinal)
  }
}
