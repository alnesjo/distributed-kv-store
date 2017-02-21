package se.kth.id2203.link;

import java.io.Serializable;

import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Transport;

public class NetworkHeader implements Serializable, Header<Address> {

    private final static long serialVersionUID = (- 1611726940513460931L);
    public final Address src;
    public final Address dst;
    public final Transport ptc;

    public NetworkHeader(Address src, Address dst, Transport ptc) {
        this.src = src;
        this.dst = dst;
        this.ptc = ptc;
    }

    @Override
    public Address getSource() {
        return src;
    }

    @Override
    public Address getDestination() {
        return dst;
    }

    @Override
    public Transport getProtocol() { return ptc; }

    @Override
    public String toString() { return this.getClass()+"("+src+","+dst+","+ptc+")"; }
}