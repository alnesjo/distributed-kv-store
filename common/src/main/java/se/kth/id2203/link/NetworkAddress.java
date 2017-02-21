package se.kth.id2203.link;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.UnsignedBytes;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import se.sics.kompics.network.Address;

public class NetworkAddress implements Serializable, Address, Comparable<NetworkAddress> {

    private final static long serialVersionUID = 2536770490757392511L;
    private final InetSocketAddress isa;

    public NetworkAddress(InetAddress addr, int portI) {
        this.isa = new InetSocketAddress(addr, portI);
    }

    @Override
    public InetAddress getIp() {
        return this.isa.getAddress();
    }

    @Override
    public int getPort() {
        return this.isa.getPort();
    }

    @Override
    public InetSocketAddress asSocket() {
        return this.isa;
    }

    @Override
    public boolean sameHostAs(Address other) {
        return this.isa.equals(other.asSocket());
    }

    @Override
    public final String toString() {
        return isa.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = ((11 * hash) + ((this.isa != null) ? this.isa.hashCode() : 0));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NetworkAddress)) {
            return false;
        }
        final NetworkAddress that = ((NetworkAddress) obj);
        return this.compareTo(that) == 0;
    }

    @Override
    public int compareTo(NetworkAddress that) {
        return ComparisonChain.start()
                .compare(this.isa.getAddress().getAddress(), that.isa.getAddress().getAddress(), UnsignedBytes.lexicographicalComparator())
                .compare(this.isa.getPort(), that.isa.getPort())
                .result();
    }
}