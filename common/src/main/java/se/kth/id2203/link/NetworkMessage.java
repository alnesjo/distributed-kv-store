package se.kth.id2203.link;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;

public class NetworkMessage implements Msg<Address, NetworkHeader>, PatternExtractor<Class, KompicsEvent> {

    public final NetworkHeader header;
    public final KompicsEvent payload;

    public NetworkMessage(Address src, Address dst, Transport ptc, KompicsEvent payload) {
        this.header = new NetworkHeader(src, dst, ptc);
        this.payload = payload;
    }

    @Override
    public NetworkHeader getHeader() {
        return this.header;
    }

    @Override
    public Address getSource() {
        return this.header.src;
    }

    @Override
    public Address getDestination() {
        return this.header.dst;
    }

    @Override
    public Transport getProtocol() {
        return this.header.ptc;
    }

    @Override
    public Class extractPattern() {
        return payload.getClass();
    }

    @Override
    public KompicsEvent extractValue() {
        return payload;
    }

    public String toString() {
        return this.getClass()+"("+header.src+","+header.dst+","+header.ptc+","+payload+")";
    }
}