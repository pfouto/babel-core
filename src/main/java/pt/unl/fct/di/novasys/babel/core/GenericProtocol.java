package pt.unl.fct.di.novasys.babel.core;

import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.exceptions.NoSuchProtocolException;
import pt.unl.fct.di.novasys.babel.handlers.*;
import pt.unl.fct.di.novasys.babel.internal.*;
import pt.unl.fct.di.novasys.babel.metrics.Metric;
import pt.unl.fct.di.novasys.babel.metrics.MetricsManager;
import pt.unl.fct.di.novasys.babel.generic.*;
import pt.unl.fct.di.novasys.channel.ChannelEvent;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class GenericProtocol {

    private static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    //TODO split in GenericConnectionlessProtocol and GenericConnectionProtocol?
    private static final Babel babel = Babel.getInstance();
    private final BlockingQueue<InternalEvent> queue;
    private final Thread executionThread;
    private final String protoName;
    private final short protoId;
    private final Map<Integer, ChannelHandlers> channels;
    private final Map<Short, TimerHandler<? extends ProtoTimer>> timerHandlers;
    private final Map<Short, RequestHandler<? extends ProtoRequest>> requestHandlers;
    private final Map<Short, ReplyHandler<? extends ProtoReply>> replyHandlers;
    private final Map<Short, NotificationHandler<? extends ProtoNotification>> notificationHandlers;
    //Debug
    ProtocolMetrics metrics = new ProtocolMetrics();
    private int defaultChannel;
    //protected ThreadMXBean tmx = ManagementFactory.getThreadMXBean();

    /**
     * Creates a generic protocol with the provided name and numeric identifier
     * and the given event queue policy.
     * <p>
     * Event queue policies can be defined to specify handling events in desired orders:
     * Eg. If multiple events are inside the queue, then timers are always processes first
     * than any other event in the queue.
     *
     * @param protoName the protocol name
     * @param protoId   the protocol numeric identifier
     * @param policy    the queue policy to use
     */
    public GenericProtocol(String protoName, short protoId, BlockingQueue<InternalEvent> policy) {
        this.queue = policy;
        this.protoId = protoId;
        this.protoName = protoName;

        //TODO change to event loop (simplifies the deliver->poll->handle process)
        //TODO only change if performance better
        this.executionThread = new Thread(this::mainLoop, protoId + "-" + protoName);
        channels = new HashMap<>();
        defaultChannel = -1;

        //Initialize maps for event handlers
        this.timerHandlers = new HashMap<>();
        this.requestHandlers = new HashMap<>();
        this.replyHandlers = new HashMap<>();
        this.notificationHandlers = new HashMap<>();

        //tmx.setThreadContentionMonitoringEnabled(true);
    }

    /**
     * Create a generic protocol with the provided name and numeric identifier
     * and network service
     * <p>
     * The internal event queue is defined to have a FIFO policy on all events
     *
     * @param protoName name of the protocol
     * @param protoId   numeric identifier
     */
    public GenericProtocol(String protoName, short protoId) {
        this(protoName, protoId, new LinkedBlockingQueue<>());
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
    public abstract void init(Properties props) throws HandlerRegistrationException, IOException;

    /**
     * Start the execution thread of the protocol
     */
    public final void start() {
        this.executionThread.start();
    }

    public ProtocolMetrics getMetrics() {
        return metrics;
    }

    protected long getMillisSinceBabelStart() {
        return babel.getMillisSinceStart();
    }

    /* ------------------ PROTOCOL REGISTERS -------------------------------------------------*/

    protected void registerMetric(Metric m) {
        MetricsManager.getInstance().registerMetric(m);
    }

    private <V> void registerHandler(short id, V handler, Map<Short, V> handlerMap)
            throws HandlerRegistrationException {
        if (handlerMap.putIfAbsent(id, handler) != null) {
            throw new HandlerRegistrationException("Conflict in registering handler for "
                    + handler.getClass().toString() + " with id " + id + ".");
        }
    }

    /**
     * Register a message inHandler for the protocol to process message events
     * form the network
     *
     * @param cId       the id of the channel
     * @param msgId     the numeric identifier of the message event
     * @param inHandler the function to process message event
     * @throws HandlerRegistrationException if a inHandler for the message id is already registered
     */
    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, null, null);
    }

    /**
     * Register a message inHandler for the protocol to process message events
     * form the network
     *
     * @param cId         the id of the channel
     * @param msgId       the numeric identifier of the message event
     * @param inHandler   the function to handle a received message event
     * @param sentHandler the function to handle a sent message event
     * @throws HandlerRegistrationException if a inHandler for the message id is already registered
     */
    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageSentHandler<V> sentHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, sentHandler, null);
    }

    /**
     * Register a message inHandler for the protocol to process message events
     * form the network
     *
     * @param cId         the id of the channel
     * @param msgId       the numeric identifier of the message event
     * @param inHandler   the function to handle a received message event
     * @param failHandler the function to handle a failed message event
     * @throws HandlerRegistrationException if a inHandler for the message id is already registered
     */

    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageFailedHandler<V> failHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, null, failHandler);
    }

    /**
     * Register a message inHandler for the protocol to process message events
     * form the network
     *
     * @param cId         the id of the channel
     * @param msgId       the numeric identifier of the message event
     * @param inHandler   the function to handle a received message event
     * @param sentHandler the function to handle a sent message event
     * @param failHandler the function to handle a failed message event
     * @throws HandlerRegistrationException if a inHandler for the message id is already registered
     */
    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageSentHandler<V> sentHandler,
                                                                         MessageFailedHandler<V> failHandler)
            throws HandlerRegistrationException {
        registerHandler(msgId, inHandler, getChannelOrThrow(cId).messageInHandlers);
        if (sentHandler != null) registerHandler(msgId, sentHandler, getChannelOrThrow(cId).messageSentHandlers);
        if (failHandler != null) registerHandler(msgId, failHandler, getChannelOrThrow(cId).messageFailedHandlers);
    }

    /**
     * Register an handler to process a channel-specific event
     *
     * @param cId     the id of the channel
     * @param eventId the id of the event to process
     * @param handler the function to handle the event
     * @throws HandlerRegistrationException if a inHandler for the event id is already registered
     */
    protected final <V extends ChannelEvent> void registerChannelEventHandler(int cId, short eventId,
                                                                              ChannelEventHandler<V> handler)
            throws HandlerRegistrationException {
        registerHandler(eventId, handler, getChannelOrThrow(cId).channelEventHandlers);
    }

    /**
     * Register a timer handler for the protocol to process timer events
     *
     * @param timerID the numeric identifier of the timer event
     * @param handler the function to process timer event
     * @throws HandlerRegistrationException if a handler for the timer timerID is already registered
     */
    protected final <V extends ProtoTimer> void registerTimerHandler(short timerID,
                                                                     TimerHandler<V> handler)
            throws HandlerRegistrationException {
        registerHandler(timerID, handler, timerHandlers);
    }

    /**
     * Register a request handler for the protocol to process request events
     *
     * @param requestId the numeric identifier of the request event
     * @param handler   the function to process request event
     * @throws HandlerRegistrationException if a handler for the request requestId is already registered
     */
    protected final <V extends ProtoRequest> void registerRequestHandler(short requestId,
                                                                         RequestHandler<V> handler)
            throws HandlerRegistrationException {
        registerHandler(requestId, handler, requestHandlers);
    }

    /**
     * Register a reply handler for the protocol to process reply events
     *
     * @param replyId the numeric identifier of the reply event
     * @param handler the function to process reply event
     * @throws HandlerRegistrationException if a handler for the reply replyId is already registered
     */
    protected final <V extends ProtoReply> void registerReplyHandler(short replyId, ReplyHandler<V> handler)
            throws HandlerRegistrationException {
        registerHandler(replyId, handler, replyHandlers);
    }

    /* ------------------------- NETWORK/CHANNELS ---------------------- */

    private ChannelHandlers getChannelOrThrow(int channelId) {
        ChannelHandlers handlers = channels.get(channelId);
        if (handlers == null)
            throw new AssertionError("Channel does not exist: " + channelId);
        return handlers;
    }

    /**
     * Registers a (de)serializer for a message type
     *
     * @param msgId      the message id
     * @param serializer the serializer for the given message id
     */
    protected final void registerMessageSerializer(int channelId, short msgId,
                                                   ISerializer<? extends ProtoMessage> serializer) {
        babel.registerSerializer(channelId, msgId, serializer);
    }

    /**
     * Creates a new channel
     *
     * @param channelName the name of the channel
     * @param props       channel-specific properties. See the documentation for each channel.
     * @return the id of the newly created channel
     */
    protected final int createChannel(String channelName, Properties props) throws IOException {
        int channelId = babel.createChannel(channelName, this.protoId, props);
        registerSharedChannel(channelId);
        return channelId;
    }

    protected final void registerSharedChannel(int channelId) {
        babel.registerChannelInterest(channelId, this.protoId, this);
        channels.put(channelId, new ChannelHandlers());
        if (defaultChannel == -1)
            setDefaultChannel(channelId);
    }

    /**
     * Sets the default channel for the {@link #sendMessage(ProtoMessage, Host)}, {@link #openConnection(Host)}
     * and {@link #closeConnection(Host)} methods.
     *
     * @param channelId the channel id
     */
    protected final void setDefaultChannel(int channelId) {
        getChannelOrThrow(channelId);
        defaultChannel = channelId;
    }

    /**
     * Sends a message to a specified destination, using the default channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(ProtoMessage msg, Host destination) {
        sendMessage(defaultChannel, msg, this.protoId, destination, 0);
    }

    /**
     * Sends a message to a specified destination using the given channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param channelId   the channel to send the message through
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(int channelId, ProtoMessage msg, Host destination) {
        sendMessage(channelId, msg, this.protoId, destination, 0);
    }

    /**
     * Sends a message to a different protocol in the specified destination, using the default channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param destProto   the target protocol for the message.
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(ProtoMessage msg, short destProto, Host destination) {
        sendMessage(defaultChannel, msg, destProto, destination, 0);
    }

    /**
     * Sends a message to a specified destination, using the default channel, and a specific connection in that channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param connection  the channel-specific connection to use.
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(ProtoMessage msg, Host destination, int connection) {
        sendMessage(defaultChannel, msg, this.protoId, destination, connection);
    }

    /**
     * Sends a message to a specified destination, using a specific connection in a given channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param channelId   the channel to send the message through
     * @param connection  the channel-specific connection to use.
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(int channelId, ProtoMessage msg, Host destination, int connection) {
        sendMessage(channelId, msg, this.protoId, destination, connection);
    }

    /**
     * Sends a message to a different protocol in the specified destination,
     * using a specific connection in the default channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param destProto   the target protocol for the message.
     * @param connection  the channel-specific connection to use.
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(ProtoMessage msg, short destProto, Host destination, int connection) {
        sendMessage(defaultChannel, msg, destProto, destination, connection);
    }

    /**
     * Sends a message to a different protocol in the specified destination,
     * using a specific connection in the given channel.
     * May require the use of {@link #openConnection(Host)} beforehand.
     *
     * @param channelId   the channel to send the message through
     * @param destProto   the target protocol for the message.
     * @param connection  the channel-specific connection to use.
     * @param msg         the message to send
     * @param destination the ip/port to send the message to
     */
    protected final void sendMessage(int channelId, ProtoMessage msg, short destProto,
                                     Host destination, int connection) {
        getChannelOrThrow(channelId);
        if (logger.isDebugEnabled())
            logger.debug("Sending: " + msg + " to " + destination + " proto " + destProto +
                    " channel " + channelId);
        babel.sendMessage(channelId, connection, new BabelMessage(msg, this.protoId, destProto), destination);
    }

    /**
     * Open a connection to the given peer using the default channel.
     * Depending on the channel, this method may be unnecessary/forbidden.
     *
     * @param peer the ip/port to create the connection to.
     */
    protected final void openConnection(Host peer) {
        openConnection(peer, defaultChannel);
    }

    /**
     * Open a connection to the given peer using the given channel.
     * Depending on the channel, this method may be unnecessary/forbidden.
     *
     * @param peer      the ip/port to create the connection to.
     * @param channelId the channel to create the connection in
     */
    protected final void openConnection(Host peer, int channelId) {
        babel.openConnection(channelId, peer);
    }

    /**
     * Closes the connection to the given peer using the default channel.
     * Depending on the channel, this method may be unnecessary/forbidden.
     *
     * @param peer the ip/port to close the connection to.
     */
    protected final void closeConnection(Host peer) {
        closeConnection(peer, defaultChannel);
    }

    /**
     * Closes the connection to the given peer in the given channel.
     * Depending on the channel, this method may be unnecessary/forbidden.
     *
     * @param peer      the ip/port to close the connection to.
     * @param channelId the channel to close the connection in
     */
    protected final void closeConnection(Host peer, int channelId) {
        closeConnection(peer, channelId, protoId);
    }

    /**
     * Closes a specific connection to the given peer in the given channel.
     * Depending on the channel, this method may be unnecessary/forbidden.
     *
     * @param peer       the ip/port to close the connection to.
     * @param channelId  the channel to close the connection in
     * @param connection the channel-specific connection to close
     */
    protected final void closeConnection(Host peer, int channelId, int connection) {
        babel.closeConnection(channelId, peer, connection);
    }

    /* ------------------ IPC BABEL PROXY -------------------------------------------------*/

    /**
     * Sends a request to the destination protocol
     *
     * @param request     request event
     * @param destination the destination protocol
     * @throws NoSuchProtocolException if the protocol does not exists
     */
    protected final void sendRequest(ProtoRequest request, short destination) throws NoSuchProtocolException {
        babel.sendIPC(new IPCEvent(request, protoId, destination));
    }

    /**
     * Sends a reply to the destination protocol
     *
     * @param destination the destination protocol
     * @param reply       reply event
     * @throws NoSuchProtocolException if the protocol does not exists
     */
    protected final void sendReply(ProtoReply reply, short destination) throws NoSuchProtocolException {
        babel.sendIPC(new IPCEvent(reply, protoId, destination));
    }

    // ------------------------------ NOTIFICATION BABEL PROXY ---------------------------------

    /**
     * Subscribes a notification, executing the given callback everytime it is triggered by any protocol.
     *
     * @param nId the id of the notification to subscribe to
     * @param h   the callback to execute upon receiving the notification
     * @throws HandlerRegistrationException if there is already a callback for the notification
     */
    protected final <V extends ProtoNotification> void subscribeNotification(short nId, NotificationHandler<V> h)
            throws HandlerRegistrationException {
        registerHandler(nId, h, notificationHandlers);
        babel.subscribeNotification(nId, this);
    }

    /**
     * Unsubscribes a notification.
     *
     * @param nId the id of the notification to unsubscribe from
     */
    protected final void unsubscribeNotification(short nId) {
        notificationHandlers.remove(nId);
        babel.unsubscribeNotification(nId, this);
    }

    /**
     * Triggers a notification, causing every protocol that subscribe it to execute its callback.
     *
     * @param n the notification event to trigger
     */
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
     * @param t       the timer event
     * @param timeout timout until trigger (in milliseconds)
     * @return unique identifier of the t set
     */
    protected long setupTimer(ProtoTimer t, long timeout) {
        return babel.setupTimer(t, this, timeout);
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

    /**
     * Used by babel to deliver channel events to protocols. Do not evoke directly.
     */
    final void deliverChannelEvent(CustomChannelEvent event) {
        queue.add(event);
    }

    /**
     * Used by babel to deliver channel messages to protocols.
     */
    final protected void deliverMessageIn(MessageInEvent msgIn) {
        queue.add(msgIn);
    }

    /**
     * Used by babel to deliver channel message sent events to protocols. Do not evoke directly.
     */
    final void deliverMessageSent(MessageSentEvent event) {
        queue.add(event);
    }

    /**
     * Used by babel to deliver channel message failed events to protocols. Do not evoke directly.
     */
    final void deliverMessageFailed(MessageFailedEvent event) {
        queue.add(event);
    }

    /**
     * Used by babel to deliver timer events to protocols. Do not evoke directly.
     */
    final void deliverTimer(TimerEvent timer) {
        queue.add(timer);
    }

    /**
     * Used by babel to deliver notifications to protocols. Do not evoke directly.
     */
    final void deliverNotification(NotificationEvent notification) {
        queue.add(notification);
    }

    /**
     * Used by babel to deliver requests/replies to protocols. Do not evoke directly.
     */
    final void deliverIPC(IPCEvent ipc) {
        queue.add(ipc);
    }

    /* ------------------ MAIN LOOP -------------------------------------------------*/

    private void mainLoop() {
        try {
            while (true) {

                InternalEvent pe = this.queue.take();
                metrics.totalEventsCount++;
                if (logger.isDebugEnabled())
                    logger.debug("Handling event: " + pe);
                switch (pe.getType()) {
                    case MESSAGE_IN_EVENT:
                        metrics.messagesInCount++;
                        this.handleMessageIn((MessageInEvent) pe);
                        break;
                    case MESSAGE_FAILED_EVENT:
                        metrics.messagesFailedCount++;
                        this.handleMessageFailed((MessageFailedEvent) pe);
                        break;
                    case MESSAGE_SENT_EVENT:
                        metrics.messagesSentCount++;
                        this.handleMessageSent((MessageSentEvent) pe);
                        break;
                    case TIMER_EVENT:
                        metrics.timersCount++;
                        this.handleTimer((TimerEvent) pe);
                        break;
                    case NOTIFICATION_EVENT:
                        metrics.notificationsCount++;
                        this.handleNotification((NotificationEvent) pe);
                        break;
                    case IPC_EVENT:
                        IPCEvent i = (IPCEvent) pe;
                        switch (i.getIpc().getType()) {
                            case REPLY:
                                metrics.repliesCount++;
                                handleReply((ProtoReply) i.getIpc(), i.getSenderID());
                                break;
                            case REQUEST:
                                metrics.requestsCount++;
                                handleRequest((ProtoRequest) i.getIpc(), i.getSenderID());
                                break;
                            default:
                                throw new AssertionError("Ups");
                        }
                        break;
                    case CUSTOM_CHANNEL_EVENT:
                        metrics.customChannelEventsCount++;
                        this.handleChannelEvent((CustomChannelEvent) pe);
                        break;
                    default:
                        throw new AssertionError("Unexpected event received by babel. protocol "
                                + protoId + " (" + protoName + ")");
                }
            }
        } catch (Exception e) {
            logger.error("Protocol " + getProtoName() + " (" + getProtoId() + ") crashed with unhandled exception " + e, e);
            e.printStackTrace();
        }
    }

    //TODO try catch (ClassCastException)
    private void handleMessageIn(MessageInEvent m) {
        BabelMessage msg = m.getMsg();
        MessageInHandler h = getChannelOrThrow(m.getChannelId()).messageInHandlers.get(msg.getMessage().getId());
        if (h != null)
            h.receive(msg.getMessage(), m.getFrom(), msg.getSourceProto(), m.getChannelId());
        else
            logger.warn("Discarding unexpected message (id " + msg.getMessage().getId() + "): " + m);
    }

    private void handleMessageFailed(MessageFailedEvent e) {
        BabelMessage msg = e.getMsg();
        MessageFailedHandler h = getChannelOrThrow(e.getChannelId()).messageFailedHandlers.get(msg.getMessage().getId());
        if (h != null)
            h.onMessageFailed(msg.getMessage(), e.getTo(), msg.getDestProto(), e.getCause(), e.getChannelId());
        else if (logger.isDebugEnabled())
            logger.debug("Discarding unhandled message failed event " + e);
    }

    private void handleMessageSent(MessageSentEvent e) {
        BabelMessage msg = e.getMsg();
        MessageSentHandler h = getChannelOrThrow(e.getChannelId()).messageSentHandlers.get(msg.getMessage().getId());
        if (h != null)
            h.onMessageSent(msg.getMessage(), e.getTo(), msg.getDestProto(), e.getChannelId());
    }

    private void handleChannelEvent(CustomChannelEvent m) {
        ChannelEventHandler h = getChannelOrThrow(m.getChannelId()).channelEventHandlers.get(m.getEvent().getId());
        if (h != null)
            h.handleEvent(m.getEvent(), m.getChannelId());
        else if (logger.isDebugEnabled())
            logger.debug("Discarding unhandled channel event (id " + m.getChannelId() + "): " + m);
    }

    private void handleTimer(TimerEvent t) {
        TimerHandler h = this.timerHandlers.get(t.getTimer().getId());
        if (h != null)
            h.uponTimer(t.getTimer().clone(), t.getUuid());
        else
            logger.warn("Discarding unexpected timer (id " + t.getTimer().getId() + "): " + t);
    }

    private void handleNotification(NotificationEvent n) {
        NotificationHandler h = this.notificationHandlers.get(n.getNotification().getId());
        if (h != null)
            h.uponNotification(n.getNotification(), n.getEmitterID());
        else
            logger.warn("Discarding unexpected notification (id " + n.getNotification().getId() + "): " + n);
    }

    private void handleRequest(ProtoRequest r, short from) {
        RequestHandler h = this.requestHandlers.get(r.getId());
        if (h != null)
            h.uponRequest(r, from);
        else
            logger.warn("Discarding unexpected request (id " + r.getId() + "): " + r);
    }

    private void handleReply(ProtoReply r, short from) {
        ReplyHandler h = this.replyHandlers.get(r.getId());
        if (h != null)
            h.uponReply(r, from);
        else
            logger.warn("Discarding unexpected reply (id " + r.getId() + "): " + r);
    }

    private static class ChannelHandlers {
        private final Map<Short, MessageInHandler<? extends ProtoMessage>> messageInHandlers;
        private final Map<Short, MessageSentHandler<? extends ProtoMessage>> messageSentHandlers;
        private final Map<Short, MessageFailedHandler<? extends ProtoMessage>> messageFailedHandlers;
        private final Map<Short, ChannelEventHandler<? extends ChannelEvent>> channelEventHandlers;

        public ChannelHandlers() {
            this.messageInHandlers = new HashMap<>();
            this.messageSentHandlers = new HashMap<>();
            this.messageFailedHandlers = new HashMap<>();
            this.channelEventHandlers = new HashMap<>();
        }
    }

    public static class ProtocolMetrics {
        private long totalEventsCount, messagesInCount, messagesFailedCount, messagesSentCount, timersCount,
                notificationsCount, requestsCount, repliesCount, customChannelEventsCount;

        @Override
        public String toString() {
            return "ProtocolMetrics{" +
                    "totalEvents=" + totalEventsCount +
                    ", messagesIn=" + messagesInCount +
                    ", messagesFailed=" + messagesFailedCount +
                    ", messagesSent=" + messagesSentCount +
                    ", timers=" + timersCount +
                    ", notifications=" + notificationsCount +
                    ", requests=" + requestsCount +
                    ", replies=" + repliesCount +
                    ", customChannelEvents=" + customChannelEventsCount +
                    '}';
        }

        public void reset() {
            totalEventsCount = messagesFailedCount = messagesInCount = messagesSentCount = timersCount =
                    notificationsCount = repliesCount = requestsCount = customChannelEventsCount = 0;
        }

        public long getCustomChannelEventsCount() {
            return customChannelEventsCount;
        }

        public long getMessagesFailedCount() {
            return messagesFailedCount;
        }

        public long getMessagesInCount() {
            return messagesInCount;
        }

        public long getMessagesSentCount() {
            return messagesSentCount;
        }

        public long getNotificationsCount() {
            return notificationsCount;
        }

        public long getRepliesCount() {
            return repliesCount;
        }

        public long getRequestsCount() {
            return requestsCount;
        }

        public long getTimersCount() {
            return timersCount;
        }

        public long getTotalEventsCount() {
            return totalEventsCount;
        }
    }
}