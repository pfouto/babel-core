package babel.protocol;

import babel.Babel;
import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.NotificationDoesNotExistException;
import babel.handlers.*;
import babel.requestreply.IReplyConsumer;
import babel.requestreply.IRequestConsumer;
import network.*;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;
import babel.protocol.event.*;
import babel.requestreply.ProtocolReply;
import babel.requestreply.ProtocolRequest;
import babel.timer.ITimerConsumer;
import babel.timer.ProtocolTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An abstract class that represent a generic protocol
 *
 * This class handles all interactions required by protocols
 *
 * Users should extend this class to implement their protocols
 */
public abstract class GenericProtocol implements IMessageConsumer, ITimerConsumer, INotificationConsumer, IRequestConsumer, IReplyConsumer, INotificationProducer, Runnable {

    private BlockingQueue<ProtocolEvent> queue;
    private Thread executionThread;
    private String protoName;
    private short protoId;
    private final INetwork network;

    protected final Host myself;

    private Map<Short, ProtocolMessageHandler> messageHandlers;
    private Map<Short, ProtocolTimerHandler> timerHandlers;
    private Map<Short, ProtocolRequestHandler> requestHandlers;
    private Map<Short, ProtocolReplyHandler> replyHandlers;
    private Map<Short, ProtocolNotificationHandler> notificationHandlers;

    private Map<String, Short> producedNotifications;
    private Map<Short, String> producedNotificationsById;

    private Map<Short, Set<INotificationConsumer>> subscribers;

    private static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    private static Babel babel = Babel.getInstance();

    /**
     * Create a generic protocol with the provided name and numeric identifier
     * and network service
     * @param protoName name of the protocol
     * @param protoID numeric identifier
     * @param net network service
     */
    public GenericProtocol(String protoName, short protoID, INetwork net) {
        this.queue = new LinkedBlockingQueue<>();
        this.protoId = protoID;
        this.protoName = protoName;
        this.network = net;
        this.myself = network.myHost();
        this.executionThread = new Thread(this, "Protocol " + protoID + " (" + protoName + ")");

        //Initialize maps for event handlers
        this.messageHandlers = new HashMap<>();
        this.timerHandlers = new HashMap<>();
        this.requestHandlers = new HashMap<>();
        this.replyHandlers = new HashMap<>();
        this.notificationHandlers = new HashMap<>();

        //initialize maps for notification management
        this.producedNotifications = new HashMap<>();
        this.producedNotificationsById = new HashMap<>();
        this.subscribers = new HashMap<>();
    }

    /**
     * Returns the numeric identifier of the protocol
     * @return numeric identifier
     */
    public final short getProtoId() {
        return protoId;
    }

    /**
     * Returns the name of the protocol
     * @return name
     */
    public final String getProtoName() {
        return protoName;
    }

    /**
     * Initializes the protocol with the given properties
     * @param props properties
     */
    public abstract void init(Properties props);

    /**
     * Start the execution thread of the protocol
     */
    public final void start() {
        this.executionThread.start();
    }

    /**
     * Register a message handler for the protocol to process message events
     * form the network
     * @param id the numeric identifier of the message event
     * @param handler the function to process message event
     * @param serializer the serializer to serialize messages to the network
     * @throws HandlerRegistrationException if a handler for the message id is already registered
     */
    protected final void registerMessageHandler(short id, ProtocolMessageHandler handler, ISerializer<? extends ProtocolMessage> serializer) throws HandlerRegistrationException {
        if(this.messageHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for message with id " + id + ".");
        network.registerConsumer(id, this);
        network.registerSerializer(id, serializer);
        this.messageHandlers.put(id, handler);
    }

    /**
     * Register a timer handler for the protocol to process timer events
     * @param id the numeric identifier of the timer event
     * @param handler the function to process timer event
     * @throws HandlerRegistrationException if a handler for the timer id is already registered
     */
    protected final void registerTimerHandler(short id, ProtocolTimerHandler handler) throws HandlerRegistrationException {
        if(this.timerHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for timer with id " + id + ".");
        this.timerHandlers.put(id, handler);
    }

    /**
     * Register a request handler for the protocol to process request events
     * @param id the numeric identifier of the request event
     * @param handler the function to process request event
     * @throws HandlerRegistrationException if a handler for the request id is already registered
     */
    protected final void registerRequestHandler(short id, ProtocolRequestHandler handler) throws HandlerRegistrationException {
        if(this.requestHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for request with id " + id + ".");
        this.requestHandlers.put(id, handler);
    }

    /**
     * Register a reply handler for the protocol to process reply events
     * @param id the numeric identifier of the reply event
     * @param handler the function to process reply event
     * @throws HandlerRegistrationException if a handler for the reply id is already registered
     */
    protected final void registerReplyHandler(short id, ProtocolReplyHandler handler) throws HandlerRegistrationException {
        if(this.replyHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for reply with id " + id + ".");
        this.replyHandlers.put(id, handler);
    }

    /**
     * Register a notification handler for the protocol to process notification events
     * @param id the numeric identifier of the notification event
     * @param handler the function to process notification event
     * @throws HandlerRegistrationException if a handler for the notification id is already registered
     */
    protected final void registerNotificationHandler(short id, ProtocolNotificationHandler handler) throws HandlerRegistrationException {
        if(this.notificationHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for notification with id " + id + ".");
        this.notificationHandlers.put(id, handler);
    }

    @Override
    public final void run() {
        try {
        while(true) {
            try {
                ProtocolEvent pe = this.queue.take();
                logger.debug("Protocol " + protoId + ": Received event of type: " + pe.getClass().getCanonicalName());
                switch(pe.getType()) {
                    case MESSAGE_EVENT:
                        this.handleMessage((ProtocolMessage) pe);
                        break;
                    case TIMER_EVENT:
                        this.handleTimer((ProtocolTimer) pe);
                        break;
                    case NOTIFICATION_EVENT:
                        this.handleNotification((ProtocolNotification) pe);
                        break;
                    case REQUEST_EVENT:
                        this.handleRequest((ProtocolRequest) pe);
                        break;
                    case REPLY_EVENT:
                        this.handleReply((ProtocolReply) pe);
                        break;
                    default:
                        throw new AssertionError("Unexpected event received by babel.protocol " + protoId + " (" + protoName + ")");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        } catch (Exception e) {
            System.err.println("Control Thread of protocol " + protoId + " has crashed.");
            e.printStackTrace();
        }
    }

    private void handleMessage(ProtocolMessage m) {
        ProtocolMessageHandler h = this.messageHandlers.get(m.getId());
        if(h == null) {
            logger.warn("Discarding unexpected message (id " + m.getId() + "): " + m );
            return;
        }
        h.receive(m);
    }

    private void handleTimer(ProtocolTimer t) {
        ProtocolTimerHandler h = this.timerHandlers.get(t.getId());
        if(h == null) {
            logger.warn("Discarding unexpected timer (id " + t.getId() + "): " + t );
            return;
        }
        h.uponTimer(t);
    }

    private void handleNotification(ProtocolNotification n) {
        ProtocolNotificationHandler h = this.notificationHandlers.get(n.getId());
        if(h == null) {
            logger.warn("Discarding unexpected notification (id " + n.getId() + "): " + n );
            return;
        }
        h.uponNotification(n);
    }

    private  void handleRequest(ProtocolRequest r) {
        ProtocolRequestHandler h = this.requestHandlers.get(r.getId());
        if(h == null) {
            logger.warn("Discarding unexpected request (id " + r.getId() + "): " + r);
            return;
        }
        h.uponRequest(r);
    }

    private void handleReply(ProtocolReply r) {
        ProtocolReplyHandler h = this.replyHandlers.get(r.getId());
        if(h == null) {
            logger.warn("Discarding unexpected reply (id " + r.getId() + "): " + r);
            return;
        }
        h.uponReply(r);
    }

    /* ------------------ Interface to send messages ---------------------- */

    /**
     * Send the provided message to the provided host destination
     * @param msg the message
     * @param destination the host destination
     */
    protected final void sendMessage(ProtocolMessage msg, Host destination) {
        logger.debug("Sending: " + msg + " to " + destination);
        network.sendMessage(msg.getId(), msg, destination);
    }

    /**
     * Send the provided message to the provided host destination, using a dedicated,
     * temporary TCP connection.
     * @param msg the message
     * @param destination the host destination
     */
    protected final void sendMessageSideChannel(ProtocolMessage msg, Host destination) {
        logger.debug("SendingSideChannel: " + msg + " to " + destination);
        network.sendMessage(msg.getId(), msg, destination, true);
    }
    /**
     * Register a listener for the network layer
     * @see INodeListener
     * @param listener the listener
     */
    protected final void registerNodeListener(INodeListener listener){
        network.registerNodeListener(listener);
    }

    /**
     * Opens a (persistent) TCP connection in the network layer to a given peer.
     * @see INetwork
     * @param peer represents the peer to which a TCP connection will be created.
     */
    protected final void addNetworkPeer(Host peer) {
        network.addPeer(peer);
    }

    /**
     * Closes the network layer TCP connection to a given peer.
     * @see INetwork
     * @param peer represents the peer to which a TCP connection will be closed.
     */
    protected final void removeNetworkPeer(Host peer) {
        network.removePeer(peer);
    }

    /* -------------------------- Interface to send requests ----------------------- */

    /**
     * Send a request to the destination protocol
     * @param request request event
     * @throws DestinationProtocolDoesNotExist if the protocol does not exists
     */
    protected final void sendRequest(ProtocolRequest request) throws DestinationProtocolDoesNotExist {
        request.setSender(this.getProtoId());
        GenericProtocol gp = babel.getProtocol(request.getDestinationID());
        if(gp == null)
            throw new DestinationProtocolDoesNotExist("Destination of Request invalid (proto: " + request.getDestinationID() + ")" );
        gp.deliverRequest(request);
    }

    /* -------------------------- Interface to send replies ----------------------- */

    /**
     * Send a reply to the destination protocol
     * @param reply reply event
     * @throws DestinationProtocolDoesNotExist if the protocol does not exists
     */
    protected final void sendReply(ProtocolReply reply) throws DestinationProtocolDoesNotExist  {
        reply.setSender(this.getProtoId());
        GenericProtocol gp = babel.getProtocol(reply.getDestinationID());
        if(gp == null)
            throw new DestinationProtocolDoesNotExist("Destination of Reply invalid (proto: " + reply.getDestinationID() + ")" );
        gp.deliverReply(reply);
    }

    /* -------------------------- Interface to manage Timers ----------------------- */

    /**
     * Setups a period timer
     * @param timer the timer event
     * @param first timeout until first trigger
     * @param period periodicity
     * @return unique identifier of the timer set
     */
    protected UUID setupPeriodicTimer(ProtocolTimer timer, long first, long period) {
        return babel.setupPeriodicTimer(this, timer, first, period);
    }

    /**
     * Setups a timer
     * @param timer the timer event
     * @param timeout timout until trigger
     * @return unique identifier of the timer set
     */
    protected UUID setupTimer(ProtocolTimer timer, long timeout) {
        return babel.setupTimer(this, timer, timeout);
    }

    /**
     * Cancel the timer with the provided unique identifier
     * @param timerID timer unique identifier
     * @return the canceled timer event, or null if it wasn't set or have already been trigger and was not periodic
     */
    protected ProtocolTimer cancelTimer(UUID timerID) {
        return babel.cancelTimer(timerID);
    }

    /**
     * Delivers a message to the protocol
     *
     * NOTE: Message comes from the network layer
     * @param msgID numeric identifier of the message
     * @param msg the message itself
     * @param from the host from which the message was sent
     */
    @Override
    public final void deliverMessage(short msgID, Object msg, Host from) {
        logger.debug("Network delivery of message type: " + msgID + " from " + from);
        ProtocolMessage pm = (ProtocolMessage) msg;
        pm.setId(msgID);
        pm.setFrom(from);
        queue.add(pm);
    }

    /**
     * Deliver the timer to the protocol
     * @param timer the timer to be delivered
     */
    @Override
    public final void deliverTimer(ProtocolTimer timer) {
        queue.add(timer);
    }


    /**
     * Deliver the notification to the protocol
     * @param notification the notification to be delivered
     */
    @Override
    public void deliverNotification(ProtocolNotification notification) {
        queue.add(notification);
    }

    /**
     * Deliver the request to the protocol
     * @param request the request to be delivered
     */
    @Override
    public void deliverRequest(ProtocolRequest request) { queue.add(request); }

    /**
     * Deliver the reply to the protocol
     * @param reply the reply to be delivered
     */
    @Override
    public void deliverReply(ProtocolReply reply) { queue.add(reply); }

    /* --------------------------- Methods from INotificationProducer --------------------- */


    /**
     * Subscribe to the notification of the protocol with the provided numeric identifier
     * to be consumed by the provided protocol
     * @param notificationID notification numeric identifier
     * @param consumerProtocol protocol to consume the notification
     * @throws NotificationDoesNotExistException if the notification is not produced
     */
    @Override
    public final void subscribeNotification(short notificationID, INotificationConsumer consumerProtocol) throws NotificationDoesNotExistException {
        if(!this.producedNotificationsById.containsKey(notificationID))
            throw new NotificationDoesNotExistException("Protocol " + this.getProtoName() + " does not produce babel.notification with id: " + notificationID);
        this.subscribers.computeIfAbsent(notificationID, k -> new HashSet<>()).add(consumerProtocol);
    }

    /**
     * Unsubscribe to the notification of the protocol with the provided numeric identifier
     * that was consumed by the provided protocol
     * @param notificationID notification numeric identifier
     * @param consumerProtocol protocol that consumed the notification
     */
    @Override
    public final void unsubscribeNotification(short notificationID, INotificationConsumer consumerProtocol) {
        this.subscribers.getOrDefault(notificationID, Collections.emptySet()).remove(consumerProtocol);
    }

    /**
     * Subscribe to the notification of the protocol with the provided name
     * to be consumed by the provided protocol
     * @param notification notification name
     * @param consumerProtocol protocol to consume the notification
     * @throws NotificationDoesNotExistException if the notification is not produced
     */
    @Override
    public void subscribeNotification(String notification, INotificationConsumer consumerProtocol) throws NotificationDoesNotExistException {
        Short s = this.producedNotifications.get(notification);
        if(s==null)
            throw new NotificationDoesNotExistException("Protocol " + this.getProtoName() + " does not produce babel.notification with name: " + notification);
        this.subscribeNotification(s,consumerProtocol);
    }

    /**
     * Unsubscribe to the notification of the protocol with the provided name
     * that was consumed by the provided protocol
     * @param notification notification name
     * @param consumerProtocol protocol that consumed the notification
     */
    @Override
    public void unsubscribeNotification(String notification, INotificationConsumer consumerProtocol) {
        Short s = this.producedNotifications.get(notification);
        if(s != null)
            this.subscribers.getOrDefault(s, Collections.emptySet()).remove(consumerProtocol);
    }

    /**
     * Register the notification with the provided numeric identifier and name
     * to be produced by the protocol
     * @param id numeric identifier
     * @param name name
     */
    protected final void registerNotification(short id, String name) {
        this.producedNotifications.put(name, id);
        this.producedNotificationsById.put(id, name);
    }

    /**
     * Returns the produced notifications of the protocol
     * in a map that contains NotificationName as key and NotificationID as value
     * @return a map with the produced notifications
     */
    @Override
    public Map<String, Short> producedNotifications() {
        return this.producedNotifications;
    }

    /**
     * Deliver the notification to all interested protocol
     * @param notification the notification to be delivered
     */
    protected final void triggerNotification(ProtocolNotification notification) {
        notification.setEmitter(this.getProtoId());
        if(this.subscribers.containsKey(notification.getId())) {
            for (INotificationConsumer c : this.subscribers.get(notification.getId())) {
                c.deliverNotification(notification);
            }
        }
    }
}