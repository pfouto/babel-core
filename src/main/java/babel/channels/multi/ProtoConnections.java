package babel.channels.multi;

import babel.generic.ProtoMessage;
import channel.ChannelListener;
import channel.base.SingleThreadedBiChannel;
import channel.tcp.ConnectionState;
import channel.tcp.events.*;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Promise;
import network.Connection;
import network.NetworkManager;
import network.data.Attributes;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static babel.channels.multi.MultiChannel.LISTEN_ADDRESS_ATTRIBUTE;

class ProtoConnections {

    private static final Logger logger = LogManager.getLogger(ProtoConnections.class);

    final static String PROTO_ID = "Protocol_ID";
    private final static int CONNECTION_OUT = 0;
    private final static int CONNECTION_IN = 1;

    //Host represents the client server socket, not the client tcp connection address!
    //client connection address is in connection.getPeer
    private final Map<Host, LinkedList<Connection<ProtoMessage>>> inConnections;
    private final Map<Host, ConnectionState<ProtoMessage>> outConnections;


    private final SingleThreadedBiChannel<ProtoMessage, ProtoMessage> channel;
    private final NetworkManager<ProtoMessage> network;
    private final ChannelListener<ProtoMessage> listener;
    private final DefaultEventExecutor loop;
    private final Attributes attributes;

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


        inConnections = new HashMap<>();
        outConnections = new HashMap<>();
    }

    void sendMessage(ProtoMessage msg, Host peer, int connection) {
        logger.debug("SendMessage " + msg + " " + peer + " " + (connection == CONNECTION_IN ? "IN" : "OUT"));
        if (connection <= CONNECTION_OUT) {

            ConnectionState<ProtoMessage> conState = outConnections.computeIfAbsent(peer, k -> {
                logger.debug("onSendMessage creating connection to: " + peer);
                return new ConnectionState<>(network.createConnection(peer, attributes, channel));
            });

            if (conState.getState() == ConnectionState.State.CONNECTING) {
                conState.getQueue().add(msg);
            } else if (conState.getState() == ConnectionState.State.CONNECTED) {
                sendWithListener(msg, peer, conState.getConnection());
            } else if (conState.getState() == ConnectionState.State.DISCONNECTING) {
                conState.getQueue().add(msg);
                conState.setState(ConnectionState.State.DISCONNECTING_RECONNECT);
            } else if (conState.getState() == ConnectionState.State.DISCONNECTING_RECONNECT) {
                conState.getQueue().add(msg);
            }

        } else if (connection == CONNECTION_IN) {
            LinkedList<Connection<ProtoMessage>> inConnList = inConnections.get(peer);
            if (inConnList != null)
                sendWithListener(msg, peer, inConnList.getLast());
            else
                listener.messageFailed(msg, peer, new IllegalArgumentException("No incoming connection"));
        } else {
            listener.messageFailed(msg, peer,
                    new IllegalArgumentException("Invalid connection: " + connection));
            logger.error("Invalid sendMessage mode " + connection);
        }
    }

    private void sendWithListener(ProtoMessage msg, Host peer, Connection<ProtoMessage> established) {
        Promise<Void> promise = loop.newPromise();
        promise.addListener(future -> {
            if (future.isSuccess()) listener.messageSent(msg, peer);
            else listener.messageFailed(msg, peer, future.cause());
        });
        established.sendMessage(msg, promise);
    }

    void disconnect(Host peer) {
        logger.debug("CloseConnection " + peer);
        ConnectionState<ProtoMessage> conState = outConnections.get(peer);
        if (conState != null) {
            if (conState.getState() == ConnectionState.State.CONNECTED || conState.getState() == ConnectionState.State.CONNECTING
                    || conState.getState() == ConnectionState.State.DISCONNECTING_RECONNECT) {
                conState.setState(ConnectionState.State.DISCONNECTING);
                conState.getQueue().clear();
                conState.getConnection().disconnect();
            }
        }
    }

    void addInboundConnection(Host clientSocket, Connection<ProtoMessage> connection) {
        LinkedList<Connection<ProtoMessage>> inConnList = inConnections.computeIfAbsent(clientSocket, k -> new LinkedList<>());
        inConnList.add(connection);
        if (inConnList.size() == 1) {
            logger.debug("InboundConnectionUp " + clientSocket);
            listener.deliverEvent(new InConnectionUp(clientSocket));
        } else {
            logger.debug("Multiple InboundConnectionUp " + inConnList.size() + clientSocket);
        }
    }

    void removeInboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        Host clientSocket;
        try {
            clientSocket = connection.getPeerAttributes().getHost(LISTEN_ADDRESS_ATTRIBUTE);
        } catch (IOException e) {
            logger.error("Inbound connection without valid listen address in connectionDown: " + e.getMessage());
            connection.disconnect();
            return;
        }

        LinkedList<Connection<ProtoMessage>> inConnList = inConnections.get(clientSocket);
        if (inConnList == null || inConnList.isEmpty())
            throw new AssertionError("No connections in InboundConnectionDown " + clientSocket);
        if (!inConnList.remove(connection))
            throw new AssertionError("No connection in InboundConnectionDown " + clientSocket);

        if (inConnList.isEmpty()) {
            logger.debug("InboundConnectionDown " + clientSocket + (cause != null ? (" " + cause) : ""));
            listener.deliverEvent(new InConnectionDown(clientSocket, cause));
            inConnections.remove(clientSocket);
        } else {
            logger.debug("Extra InboundConnectionDown " + inConnList.size() + clientSocket);
        }
    }

    void addOutboundConnection(Connection<ProtoMessage> connection) {
        logger.debug("OutboundConnectionUp " + connection.getPeer());
        ConnectionState<ProtoMessage> conState = outConnections.get(connection.getPeer());
        if (conState == null) {
            throw new AssertionError("ConnectionUp with no conState: " + connection);
        } else if (conState.getState() == ConnectionState.State.CONNECTED) {
            throw new AssertionError("ConnectionUp in CONNECTED state: " + connection);
        } else if (conState.getState() == ConnectionState.State.CONNECTING) {
            conState.setState(ConnectionState.State.CONNECTED);
            conState.getQueue().forEach(m -> sendWithListener(m, connection.getPeer(), connection));
            conState.getQueue().clear();
            listener.deliverEvent(new OutConnectionUp(connection.getPeer()));
        } else { //DISCONNECTING OR DISCONNECTING_RECONNECT
            //do nothing
        }
    }

    void removeOutboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        logger.debug("OutboundConnectionDown " + connection.getPeer() + (cause != null ? (" " + cause) : ""));
        ConnectionState<ProtoMessage> conState = outConnections.remove(connection.getPeer());
        if (conState == null) {
            throw new AssertionError("ConnectionDown with no conState: " + connection);
        } else if (conState.getState() == ConnectionState.State.CONNECTING) {
            throw new AssertionError("ConnectionDown in CONNECTING state: " + connection);
        } else if (conState.getState() == ConnectionState.State.CONNECTED) {
            listener.deliverEvent(new OutConnectionDown(connection.getPeer(), cause));
        } else if (conState.getState() == ConnectionState.State.DISCONNECTING_RECONNECT) {
            outConnections.put(connection.getPeer(), new ConnectionState<>(
                    network.createConnection(connection.getPeer(), attributes, channel), conState.getQueue()));
        }

    }

    void failedOutboundConnection(Connection<ProtoMessage> connection, Throwable cause) {
        logger.debug("OutboundConnectionFailed " + connection.getPeer() + (cause != null ? (" " + cause) : ""));

        ConnectionState<ProtoMessage> conState = outConnections.remove(connection.getPeer());
        if (conState == null) {
            throw new AssertionError("ConnectionFailed with no conState: " + connection);
        } else if (conState.getState() == ConnectionState.State.CONNECTING) {
            listener.deliverEvent(new OutConnectionFailed<>(connection.getPeer(), conState.getQueue(), cause));
        } else if (conState.getState() == ConnectionState.State.DISCONNECTING_RECONNECT) {
            outConnections.put(connection.getPeer(), new ConnectionState<>(
                    network.createConnection(connection.getPeer(), attributes, channel), conState.getQueue()));
        } else if (conState.getState() == ConnectionState.State.CONNECTED) {
            throw new AssertionError("ConnectionFailed in state: " + conState.getState() + " - " + connection);
        }
    }

    void deliverMessage(ProtoMessage msg, Connection<ProtoMessage> connection) {
        Host host;
        if (connection.isInbound())
            try {
                host = connection.getPeerAttributes().getHost(LISTEN_ADDRESS_ATTRIBUTE);
            } catch (IOException e) {
                logger.error("Inbound connection without valid listen address in deliver message: " + e.getMessage());
                connection.disconnect();
                return;
            }
        else
            host = connection.getPeer();
        logger.debug("DeliverMessage " + msg + " " + host + " " + (connection.isInbound() ? "IN" : "OUT"));
        listener.deliverMessage(msg, host);
    }
}
