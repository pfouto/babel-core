package babel;

import babel.consumers.NotificationConsumer;
import babel.consumers.IPCConsumer;
import babel.consumers.TimerConsumer;
import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.*;
import babel.internal.*;
import babel.protocol.*;
import network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An abstract class that represent a generic protocol
 * <p>
 * This class handles all interactions required by protocols
 * <p>
 * Users should extend this class to implement their protocols
 */
public abstract class GenericProtocol implements IMessageConsumer, TimerConsumer, NotificationConsumer, IPCConsumer, Runnable {

    private BlockingQueue<InternalEvent> queue;
    private Thread executionThread;
    private String protoName;
    private short protoId;
    private final INetwork network;

    protected final Host myself;

    private Map<Short, ProtoMessageHandler> messageHandlers;
    private Map<Short, ProtoTimerHandler> timerHandlers;
    private Map<Short, ProtoRequestHandler> requestHandlers;
    private Map<Short, ProtoReplyHandler> replyHandlers;
    private Map<Short, ProtoNotificationHandler> notificationHandlers;

    private static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    private static Babel babel = Babel.getInstance();

    /**
     * Create a generic protocol with the provided name and numeric identifier
     * and network service
     *
     * @param protoName name of the protocol
     * @param protoID   numeric identifier
     * @param net       network service
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
    }

    /**
     * Returns the numeric identifier of the protocol
     *
     * @return numeric identifier
     */
    public final short getProtoId() {
        return protoId;
    }

    /**
     * Returns the name of the protocol
     *
     * @return name
     */
    public final String getProtoName() {
        return protoName;
    }

    /**
     * Initializes the protocol with the given properties
     *
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
     *
     * @param id         the numeric identifier of the message event
     * @param handler    the function to process message event
     * @param serializer the serializer to serialize messages to the network
     * @throws HandlerRegistrationException if a handler for the message id is already registered
     */
    protected final void registerMessageHandler(short id, ProtoMessageHandler handler,
                                                ISerializer<? extends ProtoMessage> serializer) throws HandlerRegistrationException {
        if (this.messageHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for message with id " + id + ".");
        network.registerConsumer(id, this);
        network.registerSerializer(id, serializer);
        this.messageHandlers.put(id, handler);
    }

    /**
     * Register a timer handler for the protocol to process timer events
     *
     * @param id      the numeric identifier of the timer event
     * @param handler the function to process timer event
     * @throws HandlerRegistrationException if a handler for the timer id is already registered
     */
    protected final void registerTimerHandler(short id, ProtoTimerHandler handler) throws HandlerRegistrationException {
        if (this.timerHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for timer with id " + id + ".");
        this.timerHandlers.put(id, handler);
    }

    /**
     * Register a request handler for the protocol to process request events
     *
     * @param id      the numeric identifier of the request event
     * @param handler the function to process request event
     * @throws HandlerRegistrationException if a handler for the request id is already registered
     */
    protected final void registerRequestHandler(short id, ProtoRequestHandler handler) throws HandlerRegistrationException {
        if (this.requestHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for request with id " + id + ".");
        this.requestHandlers.put(id, handler);
    }

    /**
     * Register a reply handler for the protocol to process reply events
     *
     * @param id      the numeric identifier of the reply event
     * @param handler the function to process reply event
     * @throws HandlerRegistrationException if a handler for the reply id is already registered
     */
    protected final void registerReplyHandler(short id, ProtoReplyHandler handler) throws HandlerRegistrationException {
        if (this.replyHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for reply with id " + id + ".");
        this.replyHandlers.put(id, handler);
    }

    @Override
    public final void run() {
        while (true) {
            try {
                InternalEvent pe = this.queue.take();
                switch (pe.getType()) {
                    case MESSAGE_EVENT:
                        this.handleMessage((MessageInEvent) pe);
                        break;
                    case TIMER_EVENT:
                        this.handleTimer((TimerEvent) pe);
                        break;
                    case NOTIFICATION_EVENT:
                        this.handleNotification((NotificationEvent) pe);
                        break;
                    case IPC_EVENT:
                        this.handleIPC((IPCEvent) pe);
                        break;
                    default:
                        throw new AssertionError(
                                "Unexpected event received by babel.protocol " + protoId + " (" + protoName + ")");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(MessageInEvent m) {
        ProtoMessageHandler h = this.messageHandlers.get(m.getMsg().getId());
        if (h == null) {
            logger.warn("Discarding unexpected message (id " + m.getMsg().getId() + "): " + m);
            return;
        }
        h.receive(m.getMsg(), m.getFrom());
    }

    private void handleTimer(TimerEvent t) {
        ProtoTimerHandler h = this.timerHandlers.get(t.getTimer().getId());
        if (h == null) {
            logger.warn("Discarding unexpected timer (id " + t.getTimer().getId() + "): " + t);
            return;
        }
        h.uponTimer((ProtoTimer) t.getTimer().clone(), t.getUuid());
    }

    private void handleNotification(NotificationEvent n) {
        ProtoNotificationHandler h = this.notificationHandlers.get(n.getNotification().getId());
        if (h == null) {
            logger.warn("Discarding unexpected notification (id " + n.getNotification().getId() + "): " + n);
            return;
        }
        h.uponNotification(n.getNotification(), n.getEmitterID());
    }

    private void handleIPC(IPCEvent i){
        switch (i.getIpc().getType()){
            case REPLY:
                handleReply((ProtoReply) i.getIpc(), i.getSenderID());
                break;
            case REQUEST:
                handleRequest((ProtoRequest) i.getIpc(), i.getSenderID());
                break;
            default:
                throw new AssertionError("UPS");
        }
    }

    private void handleRequest(ProtoRequest r, short from) {
        ProtoRequestHandler h = this.requestHandlers.get(r.getId());
        if (h == null) {
            logger.warn("Discarding unexpected request (id " + r.getId() + "): " + r);
            return;
        }
        h.uponRequest(r, from);
    }

    private void handleReply(ProtoReply r, short from) {
        ProtoReplyHandler h = this.replyHandlers.get(r.getId());
        if (h == null) {
            logger.warn("Discarding unexpected reply (id " + r.getId() + "): " + r);
            return;
        }
        h.uponReply(r, from);
    }

    /* ------------------------- NETWORK ---------------------- */

    /**
     * Send the provided message to the provided host destination
     *
     * @param msg         the message
     * @param destination the host destination
     */
    protected final void sendMessage(ProtoMessage msg, Host destination) {
        network.sendMessage(msg.getId(), msg, destination);
    }

    /**
     * Register a listener for the network layer
     *
     * @param listener the listener
     * @see INodeListener
     */
    protected final void registerNodeListener(INodeListener listener) {
        network.registerNodeListener(listener);
    }

    /**
     * Opens a (persistent) TCP connection in the network layer to a given peer.
     *
     * @param peer represents the peer to which a TCP connection will be created.
     * @see INetwork
     */
    protected final void addNetworkPeer(Host peer) {
        network.addPeer(peer);
    }

    /**
     * Closes the network layer TCP connection to a given peer.
     *
     * @param peer represents the peer to which a TCP connection will be closed.
     * @see INetwork
     */
    protected final void removeNetworkPeer(Host peer) {
        network.removePeer(peer);
    }

    /* ---------------------------------------- REQ / REPLY ------------------------------------- */

    /**
     * Send a request to the destination protocol
     *
     * @param request request event
     * @throws DestinationProtocolDoesNotExist if the protocol does not exists
     */
    protected final void sendRequest(ProtoRequest request, short destination) throws DestinationProtocolDoesNotExist {
        babel.sendIPC(new IPCEvent(request, protoId, destination));
    }

    /**
     * Send a reply to the destination protocol
     *
     * @param reply reply event
     * @throws DestinationProtocolDoesNotExist if the protocol does not exists
     */
    protected final void sendReply(ProtoReply reply, short destination) throws DestinationProtocolDoesNotExist {
        babel.sendIPC(new IPCEvent(reply, protoId, destination));
    }

    /* -------------------------- TIMERS ----------------------- */

    /**
     * Setups a period timer
     *
     * @param timer  the timer event
     * @param first  timeout until first trigger (in milliseconds)
     * @param period periodicity (in milliseconds)
     * @return unique identifier of the timer set
     */
    protected long setupPeriodicTimer(ProtoTimer timer, long first, long period) {
        return babel.setupPeriodicTimer(timer, this, first, period);
    }

    /**
     * Setups a timer
     *
     * @param timer   the timer event
     * @param timeout timout until trigger (in milliseconds)
     * @return unique identifier of the timer set
     */
    protected long setupTimer(ProtoTimer timer, long timeout) {
        return babel.setupTimer(timer, this, timeout);
    }

    /**
     * Cancel the timer with the provided unique identifier
     *
     * @param timerID timer unique identifier
     * @return the canceled timer event, or null if it wasn't set or have already been trigger and was not periodic
     */
    protected ProtoTimer cancelTimer(long timerID) {
        return babel.cancelTimer(timerID);
    }

    // ------------------------------ NOTIFICATIONS ---------------------------------

    protected final void subscribeNotification(short nId, ProtoNotificationHandler h) throws HandlerRegistrationException {
        ProtoNotificationHandler oldHandler = notificationHandlers.putIfAbsent(nId, h);
        if (oldHandler != null)
            throw new HandlerRegistrationException("Conflict in registering handler for notification with id " + nId);
        babel.subscribeNotification(nId, this);
    }

    protected final void unsubscribeNotification(short nId) {
        notificationHandlers.remove(nId);
        babel.unsubscribeNotification(nId, this);
    }

    protected final void triggerNotification(ProtoNotification n) {
        babel.triggerNotification(new NotificationEvent(n, protoId));
    }

    // --------------------------------- DELIVERERS ------------------------------------

    /**
     * Delivers a message to the protocol
     * <p>
     * NOTE: Message comes from the network layer
     *
     * @param msgID numeric identifier of the message
     * @param msg   the message itself
     * @param from  the host from which the message was sent
     */
    @Override
    public final void deliverMessage(short msgID, Object msg, Host from) {
        queue.add(new MessageInEvent((ProtoMessage) msg, from));
    }

    /**
     * Deliver the timer to the protocol
     *
     * @param timer the timer to be delivered
     */
    @Override
    public final void deliverTimer(TimerEvent timer) {
        queue.add(timer);
    }

    /**
     * Deliver the notification to the protocol
     *
     * @param notification the notification to be delivered
     */
    @Override
    public void deliverNotification(NotificationEvent notification) {
        queue.add(notification);
    }

    @Override
    public void deliverIPC(IPCEvent ipc) {
        queue.add(ipc);
    }
}