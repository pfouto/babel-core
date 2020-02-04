package babel.channels.multi;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.base.SingleThreadedBiChannel;
import channel.tcp.events.*;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Promise;
import network.Connection;
import network.NetworkManager;
import network.data.Attributes;
import network.data.Host;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

class ProtoConnections {

    private static final Logger logger = LogManager.getLogger(ProtoConnections.class);

    final static String PROTO_ID = "Protocol_ID";
    private final static int MODE_OUT = 0;
    private final static int MODE_IN = 1;

    private Map<Host, Pair<Connection<ProtoMessage>, Queue<ProtoMessage>>> pendingOut;
    private Map<Host, Connection<ProtoMessage>> establishedOut;

    private BidiMap<Host, Connection<ProtoMessage>> establishedIn;

    private SingleThreadedBiChannel<ProtoMessage, ProtoMessage> channel;
    private NetworkManager<ProtoMessage> network;
    private ChannelListener<ProtoMessage> listener;
    private DefaultEventExecutor loop;
    private Attributes attributes;

    ProtoConnections(DefaultEventExecutor loop, short protoId, Attributes attributes,
                     ChannelListener<ProtoMessage> listener,
                     NetworkManager<ProtoMessage> network,
                     SingleThreadedBiChannel<ProtoMessage, ProtoMessage> channel) {

        this.channel = channel;
        this.network = network;
        this.listener = listener;
        this.loop = loop;
        this.attributes = attributes.deepClone();
        this.attributes.putShort(PROTO_ID, protoId);


        pendingOut = new HashMap<>();
        establishedOut = new HashMap<>();
        establishedIn = new DualHashBidiMap<>();
    }

    void sendMessage(ProtoMessage msg, Host peer, int mode) {
        if (mode <= MODE_OUT) {
            Connection<ProtoMessage> established = establishedOut.get(peer);
            if (established != null) {
                Promise<Void> promise = loop.newPromise();
                promise.addListener(future -> {
                    if (!future.isSuccess())
                        listener.messageFailed(msg, peer, future.cause());
                    else
                        listener.messageSent(msg, peer);
                });
                established.sendMessage(msg, promise);
            } else {
                Pair<Connection<ProtoMessage>, Queue<ProtoMessage>> pair = pendingOut.computeIfAbsent(peer, k ->
                        Pair.of(network.createConnection(peer, attributes, channel), new LinkedList<>()));
                pair.getValue().add(msg);
            }
        } else if (mode == MODE_IN) {
            Connection<ProtoMessage> inConn = establishedIn.get(peer);
            if (inConn != null) {
                Promise<Void> promise = loop.newPromise();
                promise.addListener(future -> {
                    if (!future.isSuccess())
                        listener.messageFailed(msg, peer, future.cause());
                    else
                        listener.messageSent(msg, peer);
                });
                inConn.sendMessage(msg, promise);
            } else {
                logger.error("Unable to send message, no incoming connection from " + peer + " - " + msg);
            }
        } else {
            logger.error("Invalid sendMessage mode " + mode);
        }
    }

    void disconnect(Host peer) {
        logger.debug("Disconnect to " + peer + " received");
        Pair<Connection<ProtoMessage>, Queue<ProtoMessage>> remove = pendingOut.remove(peer);
        if (remove != null) remove.getKey().disconnect();

        Connection<ProtoMessage> established = establishedOut.get(peer);
        if (established != null) established.disconnect();
    }

    void addInboundConnection(Host clientSocket, Connection<ProtoMessage> connection) {
        if (establishedIn.putIfAbsent(clientSocket, connection) != null)
            throw new RuntimeException("Double incoming connection from: " + connection.getPeer());

        listener.deliverEvent(new InConnectionUp(clientSocket));
    }

    void removeInboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        Host host = establishedIn.removeValue(connection);
        logger.debug("Inbound down: " + connection + " " + host + " " + cause);
        listener.deliverEvent(new InConnectionDown(host, cause));
    }

    void addOutboundConnection(Connection<ProtoMessage> connection) {
        Pair<Connection<ProtoMessage>, Queue<ProtoMessage>> remove = pendingOut.remove(connection.getPeer());
        if (remove == null) throw new RuntimeException("Pending null in connection up");
        logger.debug("Outbound established: " + connection);


        Connection<ProtoMessage> put = establishedOut.put(connection.getPeer(), connection);
        if (put != null) throw new RuntimeException("Connection already exists in connection up");

        listener.deliverEvent(new OutConnectionUp(connection.getPeer()));

        for (ProtoMessage msg : remove.getValue()) {
            Promise<Void> promise = loop.newPromise();
            promise.addListener(future -> {
                if (!future.isSuccess())
                    listener.messageFailed(msg, connection.getPeer(), future.cause());
                else
                    listener.messageSent(msg, connection.getPeer());
            });
            connection.sendMessage(msg, promise);
        }
    }

    void removeOutboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        logger.debug("Outbound down: " + connection);
        Connection<ProtoMessage> remove = establishedOut.remove(connection.getPeer());
        if (remove == null) throw new RuntimeException("Connection down with no context available");

        listener.deliverEvent(new OutConnectionDown(connection.getPeer(), cause));

    }

    void failedOutboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        if (establishedOut.containsKey(connection.getPeer()))
            throw new RuntimeException("Connection exists in conn failed");

        Pair<Connection<ProtoMessage>, Queue<ProtoMessage>> remove = pendingOut.remove(connection.getPeer());
        if (remove == null) throw new RuntimeException("Connection failed with no pending");

        listener.deliverEvent(new OutConnectionFailed<>(connection.getPeer(), remove.getRight(), cause));
    }

    void deliverMessage(ProtoMessage protoMessage, Connection<ProtoMessage> connection) {
        Host host;
        if(connection.isInbound())
            host = establishedIn.getKey(connection);
        else
            host = connection.getPeer();
        listener.deliverMessage(protoMessage, host);
    }
}
