package se.kth.id2203.link;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Transport;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

public class NetworkSerializer implements Serializer {

    final static Logger log = LoggerFactory.getLogger(NetworkSerializer.class);

    private static final byte ADDR = 1;
    private static final byte HEADER = 2;
    private static final byte MESSAGE = 3;

    @Override
    public int identifier() {
        return 100;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        if (o instanceof NetworkAddress) {
            NetworkAddress addr = (NetworkAddress) o;
            log.trace("Serializing {}.", o);
            buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
            buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (let's hope it's IPv4^^)
            buf.writeShort(addr.getPort()); // we only need 2 bytes here
        } else if (o instanceof NetworkHeader) {
            NetworkHeader header = (NetworkHeader) o;
            log.trace("Serializing {}.", o);
            buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
            this.toBinary(header.src, buf); // use this serialiser again (7 bytes)
            this.toBinary(header.dst, buf); // use this serialiser again (7 bytes)
            buf.writeByte(header.ptc.ordinal()); // 1 byte is enough
        } else if (o instanceof NetworkMessage) {
            NetworkMessage message = (NetworkMessage) o;
            log.trace("Serializing {}.", o);
            buf.writeByte(MESSAGE); // mark which type we are serialising (1 byte)
            this.toBinary(message.getHeader(), buf); // use this serialiser again (16 bytes)
            Serializers.toBinary(message.payload, buf); // unknown but should be serializable
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); // read the first byte to figure out the type
        switch (type) {
            case ADDR: {
                log.trace("Deserializing {}.", NetworkAddress.class);
                byte[] ipBytes = new byte[4];
                buf.readBytes(ipBytes);
                try {
                    InetAddress ip = InetAddress.getByAddress(ipBytes); // 4 bytes
                    int port = buf.readUnsignedShort(); // 2 bytes
                    NetworkAddress o = new NetworkAddress(ip, port); // total of 7, check
                    log.trace("Deserialized {}.", o);
                    return o;
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex); // let Netty deal with this
                }
            }
            case HEADER: {
                log.trace("Deserializing {}.", NetworkHeader.class);
                NetworkAddress src = (NetworkAddress) this.fromBinary(buf, Optional.absent()); // We already know what it's going to be (7 bytes)
                NetworkAddress dst = (NetworkAddress) this.fromBinary(buf, Optional.absent()); // same here (7 bytes)
                int protoOrd = buf.readByte(); // 1 byte
                Transport proto = Transport.values()[protoOrd];
                NetworkHeader o = new NetworkHeader(src, dst, proto); // total of 16 bytes, check
                log.trace("Deserialized {}.", o);
                return o;
            }
            case MESSAGE: {
                log.trace("Deserializing {}.", NetworkMessage.class);
                NetworkHeader header = (NetworkHeader) this.fromBinary(buf, Optional.absent()); // 16 bytes
                KompicsEvent payload = (KompicsEvent) Serializers.fromBinary(buf, Optional.absent()); // unknown
                NetworkMessage o = new NetworkMessage(header.getSource(), header.getDestination(), header.getProtocol(), payload); // unknown
                log.trace("Deserialized {}.", o);
                return o;
            }
        }
        return null; // strange things happened^^
    }
}
