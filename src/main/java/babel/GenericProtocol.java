package babel;

import babel.consumers.*;
import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.*;
import babel.internal.*;
import babel.protocol.*;
import network.*;
import network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An abstract class that represent a generic protocol
 * <p>
 * This class handles all interactions required by protocols
 * <p>
 * Users should extend this class to implement their protocols
 */
public abstract class GenericProtocol implements ProtoConsumers {

    //TODO split in GenericConnectionlessProtocol and GenericConnectionProtocol

    private BlockingQueue<InternalEvent> queue;
    private Thread executionThread;
    private String protoName;
    private short protoId;

    private Set<Integer> channelSet;
    private int defaultChannel;

    private Map<Short, ProtoMessageHandler<? extends ProtoMessage>> messageHandlers;
    private Map<Short, ProtoTimerHandler> timerHandlers;
    private Map<Short, ProtoRequestHandler> requestHandlers;
    private Map<Short, ProtoReplyHandler> replyHandlers;
    private Map<Short, ProtoNotificationHandler> notificationHandlers;
    private Map<Short, ProtoChannelEventHandler> channelEventHandlers;

    private static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    private static Babel babel = Babel.getInstance();

    /**
     * Create a generic protocol with the provided name and numeric identifier
     * and network service
     *
     * @param protoName name of the protocol
     * @param protoId   numeric identifier
     */
    public GenericProtocol(String protoName, short protoId) {
        this.queue = new LinkedBlockingQueue<>();
        this.protoId = protoId;
        this.protoName = protoName;

        //TODO change to event loop (simplifies the deliver->poll->handle process)
        this.executionThread = new Thread(this::mainLoop, protoId + " - " + protoName);
        channelSet = new HashSet<>();
        defaultChannel = -1;

        //Initialize maps for event handlers
        this.messageHandlers = new HashMap<>();
        this.timerHandlers = new HashMap<>();
        this.requestHandlers = new HashMap<>();
        this.replyHandlers = new HashMap<>();
        this.notificationHandlers = new HashMap<>();
        this.channelEventHandlers = new HashMap<>();
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

    /* ------------------ PROTOCOL REGISTERS -------------------------------------------------*/

    /**
     * Register a message handler for the protocol to process message events
     * form the network
     *
     * @param id         the numeric identifier of the message event
     * @param handler    the function to process message event
     * @param serializer the serializer to serialize messages to the network
     * @throws HandlerRegistrationException if a handler for the message id is already registered
     */
    protected final void registerMessageHandler(short id, ProtoMessageHandler<? extends ProtoMessage> handler,
                                                ISerializer<ProtoMessage> serializer)
            throws HandlerRegistrationException {
        if (this.messageHandlers.putIfAbsent(id, handler) != null)
            throw new HandlerRegistrationException("Conflict in registering handler for message with id " + id + ".");
        babel.registerSerializer(id, serializer);
    }

    protected final void registerChannelEventHandler(short id, ProtoChannelEventHandler handler)
            throws HandlerRegistrationException {
        if (this.channelEventHandlers.putIfAbsent(id, handler) != null)
            throw new HandlerRegistrationException("Conflict in registering handler for channel event with id " + id);
    }

    /**
     * Register a timer handler for the protocol to process timer events
     *
     * @param id      the numeric identifier of the timer event
     * @param handler the function to process timer event
     * @throws HandlerRegistrationException if a handler for the timer id is already registered
     */
    protected final void registerTimerHandler(short id, ProtoTimerHandler handler)
            throws HandlerRegistrationException {
        if (this.timerHandlers.putIfAbsent(id, handler) != null)
            throw new HandlerRegistrationException("Conflict in registering handler for timer with id " + id + ".");
    }

    /**
     * Register a request handler for the protocol to process request events
     *
     * @param id      the numeric identifier of the request event
     * @param handler the function to process request event
     * @throws HandlerRegistrationException if a handler for the request id is already registered
     */
    protected final void registerRequestHandler(short id, ProtoRequestHandler handler)
            throws HandlerRegistrationException {
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
    protected final void registerReplyHandler(short id, ProtoReplyHandler handler)
            throws HandlerRegistrationException {
        if (this.replyHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for reply with id " + id + ".");
        this.replyHandlers.put(id, handler);
    }

    /**
     * ------------------ MAIN LOOP -------------------------------------------------
     **/
    public final void mainLoop() {
        while (true) {
            try {
                InternalEvent pe = this.queue.take();
                switch (pe.getType()) {
                    case MESSAGE_IN_EVENT:
                        this.handleMessageIn((MessageInEvent) pe);
                        break;
                    case MESSAGE_FAILED_EVENT:
                        this.handleMessageFailed((MessageFailedEvent) pe);
                        break;
                    case MESSAGE_SENT_EVENT:
                        this.handleMessageSent((MessageSentEvent) pe);
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
                    case CUSTOM_CHANNEL_EVENT:
                        this.handleChannelEvent((CustomChannelEventEvent) pe);
                        break;
                    default:
                        throw new AssertionError("Unexpected event received by babel.protocol "
                                + protoId + " (" + protoName + ")");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessageIn(MessageInEvent m) {
        //TODO change everything to this?
        //TODO cannot seem to use properly with "this::function" in the protocol
        //TODO how to solve warnings? (if possible at all)


        logger.debug("Receiving: " + m.getMsg() + " from " + m.getFrom());
        ProtoMessage msg = m.getMsg().getMsg();
        ProtoMessageHandler h = this.messageHandlers.get(msg.getId());
        if (h != null)
            try {
                h.receive(msg, m.getFrom(), m.getMsg().getSourceProto(), m.getChannelId());
                //TODO apanhar este catch no main loop
            } catch (ClassCastException e){
                logger.error("Error in message handler with id: " + msg.getId());
                e.printStackTrace();
                System.exit(1);
            }
        else
            logger.warn("Discarding unexpected message (id " + m.getMsg().getMsg().getId() + "): " + m);
    }

    private void handleMessageFailed(MessageFailedEvent e) {
        onMessageFailed(e.getMsg().getMsg(), e.getTo(), e.getMsg().getDestProto(), e.getCause(), e.getChannelId());
    }
    protected abstract void onMessageFailed(ProtoMessage msg, Host to, short destProto, Throwable cause, int channelId);

    private void handleMessageSent(MessageSentEvent e) {
        onMessageSent(e.getMsg().getMsg(), e.getTo(), e.getMsg().getDestProto(), e.getChannelId());
    }
    protected abstract void onMessageSent(ProtoMessage msg, Host to, short destProto, int channelId);

    private void handleChannelEvent(CustomChannelEventEvent m) {
        logger.debug("Receiving: " + m);
        ProtoChannelEventHandler h = this.channelEventHandlers.get(m.getEvent().getId());
        if (h == null) {
            logger.warn("Discarding channel event (id " + m.getChannelId() + "): " + m);
            return;
        }
        h.handleEvent(m.getEvent(), m.getChannelId());
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

    private void handleIPC(IPCEvent i) {
        switch (i.getIpc().getType()) {
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

    /* ------------------------- NETWORK/CHANNELS ---------------------- */

    protected final int getChannel(String channelName, Properties props) throws IOException {
        int channelId = babel.createChannel(channelName, this.protoId, this, props);
        channelSet.add(channelId);
        if (defaultChannel == -1) defaultChannel = channelId;
        return channelId;
    }

    protected final void setDefaultChannel(int channelId) {
        if (!channelSet.contains(channelId))
            throw new AssertionError("Trying to set as default a not registered channel " + channelId);
        defaultChannel = channelId;
    }

    protected final void sendMessage(ProtoMessage msg, Host destination) {
        sendMessage(msg, this.protoId, destination, defaultChannel);
    }

    protected final void sendMessage(ProtoMessage msg, Host destination, int channel) {
        sendMessage(msg, this.protoId, destination, channel);
    }

    protected final void sendMessage(ProtoMessage msg, short destProto, Host destination) {
        sendMessage(msg, destProto, destination, defaultChannel);
    }

    protected final void sendMessage(ProtoMessage msg, short destProto, Host destination, int channelId) {
        if (!channelSet.contains(channelId))
            throw new AssertionError("Trying to send message to invalid channel " + channelId);

        logger.debug("Sending: " + msg + " to " + destination + " proto " + destProto + " channel " + channelId);
        babel.sendMessage(channelId, new AddressedMessage(msg, this.protoId, destProto), destination);
    }

    protected final void closeConnection(Host peer) {
        closeConnection(peer, defaultChannel);
    }

    protected final void closeConnection(Host peer, int channelId) {
        if (!channelSet.contains(channelId))
            throw new AssertionError("Trying to close connection in invalid channel " + channelId);
        babel.closeConnection(channelId, peer);
    }

    /* ------------------ IPC BABEL PROXY -------------------------------------------------*/

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

    // ------------------------------ NOTIFICATION BABEL PROXY ---------------------------------
    protected final void subscribeNotification(short nId, ProtoNotificationHandler h)
            throws HandlerRegistrationException {
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

    /* -------------------------- TIMER BABEL PROXY ----------------------- */

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

    // --------------------------------- DELIVERERS FROM BABEL ------------------------------------


    @Override
    public final void deliverChannelEvent(CustomChannelEventEvent event) {
        queue.add(event);
    }

    /**
     * Delivers a message to the protocol
     */
    @Override
    public final void deliverMessageIn(MessageInEvent msgIn) {
        queue.add(msgIn);
    }

    @Override
    public final void deliverMessageSent(MessageSentEvent event){
        queue.add(event);
    }

    @Override
    public final void deliverMessageFailed(MessageFailedEvent event){
        queue.add(event);
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