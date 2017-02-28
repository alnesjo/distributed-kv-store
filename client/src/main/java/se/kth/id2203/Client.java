/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.kth.id2203.kvstore.OperationInvoke;
import se.kth.id2203.kvstore.OperationRespond;
import se.kth.id2203.link.*;
import se.kth.id2203.overlay.Ack;
import se.kth.id2203.overlay.Connect;
import se.kth.id2203.overlay.RouteMessage;
import se.sics.kompics.Kompics;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.config.ValueMerger;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.netty.serialization.Serializers;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class Client {

    static final NetworkAddressConverter NAC = new NetworkAddressConverter();

    static {
        // conversions
        Conversions.register(NAC);

        Serializers.register(new PicklingSerializer(), "PS");
        Serializers.register(NetworkAddress.class, "PS");
        Serializers.register(NetworkHeader.class, "PS");
        Serializers.register(NetworkMessage.class, "PS");
        Serializers.register(Connect.class, "PS");
        Serializers.register(Ack.class, "PS");
        Serializers.register(OperationInvoke.class, "PS");
        Serializers.register(OperationRespond.class, "PS");
        Serializers.register(RouteMessage.class, "PS");

    }

    public static void main(String[] args) {
        Options opts = prepareOptions();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            CommandLineParser cliparser = new DefaultParser();
            cmd = cliparser.parse(opts, args);
            // avoid constant conversion of the address by converting once and reassigning
            Config.Impl c = (Config.Impl) Kompics.getConfig();
            Address self = c.getValue("id2203.project.address", NetworkAddress.class);
            Config.Builder cb = c.modify(UUID.randomUUID());
            if (cmd.hasOption("p") || cmd.hasOption("i")) {
                String ip = self.asSocket().getHostString();
                int port = self.getPort();
                if (cmd.hasOption("p")) {
                    port = Integer.parseInt(cmd.getOptionValue("p"));
                }
                if (cmd.hasOption("i")) {
                    ip = cmd.getOptionValue("i");
                }
                self = new NetworkAddress(new InetSocketAddress(InetAddress.getByName(ip), port));
            }
            cb.setValue("id2203.project.address", self);
            if (cmd.hasOption("b")) {
                String serverS = cmd.getOptionValue("b");
                Address server = NAC.convert(serverS);
                if (server == null) {
                    System.err.println("Couldn't parse address string: " + serverS);
                    System.exit(1);
                }
                cb.setValue("id2203.project.bootstrap-address", server);
            }
            ConfigUpdate cu = cb.finalise();
            c.apply(cu, ValueMerger.NONE);
            Kompics.createAndStart(ClientHost.class);
            Kompics.waitForTermination();
        } catch (ParseException ex) {
            System.err.println("Invalid commandline options: " + ex.getMessage());
            formatter.printHelp("... <options>", opts);
            System.exit(1);
        } catch (UnknownHostException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Options prepareOptions() {
        Options opts = new Options();

        opts.addOption("b", true, "Set Bootstrap server to <arg> (ip:port)");
        opts.addOption("p", true, "Changle local port to <arg> (default from config file)");
        opts.addOption("i", true, "Changle local ip to <arg> (default from config file)");
        return opts;
    }
}

