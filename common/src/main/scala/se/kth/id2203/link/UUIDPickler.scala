package se.kth.id2203.link

import java.util.UUID

import scala.pickling._

object UUIDPickler extends Pickler[UUID] with Unpickler[UUID] with pickler.PrimitivePicklers {

  override val tag = FastTypeTag[UUID]

  override def pickle(picklee: UUID, builder: PBuilder): Unit = {
    builder.hintTag(tag) // This is always required
    builder.beginEntry(picklee)
    builder.putField("most", { fieldBuilder =>
      fieldBuilder.hintTag(longPickler.tag)
      fieldBuilder.hintStaticallyElidedType()
      longPickler.pickle(picklee.getMostSignificantBits, fieldBuilder)
    })
    builder.putField("least", { fieldBuilder =>
      fieldBuilder.hintTag(longPickler.tag)
      fieldBuilder.hintStaticallyElidedType()
      longPickler.pickle(picklee.getLeastSignificantBits, fieldBuilder)
    })
    builder.endEntry()
  }

  override def unpickle(tag: String, reader: PReader): Any = {
    val mostReader = reader.readField("most")
    mostReader.hintStaticallyElidedType()
    val most = longPickler.unpickleEntry(mostReader).asInstanceOf[Long]
    val leastReader = reader.readField("least")
    leastReader.hintStaticallyElidedType()
    val least = longPickler.unpickleEntry(leastReader).asInstanceOf[Long]
    new UUID(most,least)
  }
}
