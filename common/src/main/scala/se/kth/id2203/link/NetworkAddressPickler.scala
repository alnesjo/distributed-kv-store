package se.kth.id2203.link

import scala.pickling._
import java.net.{InetAddress, InetSocketAddress}

object NetworkAddressPickler extends Pickler[NetworkAddress] with Unpickler[NetworkAddress] with pickler.PrimitivePicklers {

  override val tag = FastTypeTag[NetworkAddress]

  override def pickle(picklee: NetworkAddress, builder: PBuilder): Unit = {
    builder.hintTag(tag) // This is always required
    builder.beginEntry(picklee)
//    builder.putField("ip", { fieldBuilder =>
//      fieldBuilder.hintTag(byteArrayPickler.tag)
//      fieldBuilder.hintStaticallyElidedType()
//      byteArrayPickler.pickle(picklee.isa.getAddress.getAddress, fieldBuilder)
//    })
    builder.putField("ip", { fieldBuilder =>
      fieldBuilder.hintTag(stringPickler.tag)
      fieldBuilder.hintStaticallyElidedType()
      stringPickler.pickle(picklee.isa.getAddress.getHostAddress, fieldBuilder)
    })
    builder.putField("port", { fieldBuilder =>
      fieldBuilder.hintTag(intPickler.tag)
      fieldBuilder.hintStaticallyElidedType()
      intPickler.pickle(picklee.isa.getPort, fieldBuilder)
    })
    builder.endEntry()
  }

  override def unpickle(tag: String, reader: PReader): Any = {
    val ipReader = reader.readField("ip")
    ipReader.hintStaticallyElidedType()
    //val ip = byteArrayPickler.unpickleEntry(ipReader).asInstanceOf[Array[Byte]]
    val ip = stringPickler.unpickleEntry(ipReader).asInstanceOf[String]
    val portReader = reader.readField("port")
    portReader.hintStaticallyElidedType()
    val port = intPickler.unpickleEntry(portReader).asInstanceOf[Int]
    new NetworkAddress(new InetSocketAddress(InetAddress.getByName(ip), port))
  }
}
