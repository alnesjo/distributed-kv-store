package se.kth.id2203.link;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.net.InetAddress;
import java.net.UnknownHostException;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Transport;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

public class NetworkSerializer implements Serializer {

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
            buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
            buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (let's hope it's IPv4^^)
            buf.writeShort(addr.getPort()); // we only need 2 bytes here
            // total 7 bytes
        } else if (o instanceof NetworkHeader) {
            NetworkHeader header = (NetworkHeader) o;
            buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
            this.toBinary(header.getProtocol(), buf); // use this serialiser again (7 bytes)
            this.toBinary(header.getDestination(), buf); // use this serialiser again (7 bytes)
            buf.writeByte(header.getProtocol().ordinal()); // 1 byte is enough
            // total 16 bytes
        } else if (o instanceof NetworkMessage) {
            NetworkMessage message = (NetworkMessage) o;
            buf.writeByte(MESSAGE); // mark which type we are serialising (1 byte)
            this.toBinary(message.getHeader(), buf); // use this serialiser again (16 bytes)
            Serializers.toBinary(message.payload, buf); // unknown but should be serializable
            // total unknown
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); // read the first byte to figure out the type
        switch (type) {
            case ADDR: {
                byte[] ipBytes = new byte[4];
                buf.readBytes(ipBytes);
                try {
                    InetAddress ip = InetAddress.getByAddress(ipBytes); // 4 bytes
                    int port = buf.readUnsignedShort(); // 2 bytes
                    return new NetworkAddress(ip, port); // total of 7, check
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex); // let Netty deal with this
                }
            }
            case HEADER: {
                NetworkAddress src = (NetworkAddress) this.fromBinary(buf, Optional.absent()); // We already know what it's going to be (7 bytes)
                NetworkAddress dst = (NetworkAddress) this.fromBinary(buf, Optional.absent()); // same here (7 bytes)
                int protoOrd = buf.readByte(); // 1 byte
                Transport proto = Transport.values()[protoOrd];
                return new NetworkHeader(src, dst, proto); // total of 16 bytes, check
            }
            case MESSAGE: {
                NetworkHeader header = (NetworkHeader) this.fromBinary(buf, Optional.absent()); // 16 bytes
                KompicsEvent payload = (KompicsEvent) Serializers.fromBinary(buf, Optional.absent()); // unknown
                return new NetworkMessage(header.getSource(), header.getDestination(), header.getProtocol(), payload); // unknown
            }
        }
        return null; // strange things happened^^
    }
}
