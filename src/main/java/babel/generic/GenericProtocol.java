package babel.generic;

import babel.Babel;
import babel.events.*;
import babel.exceptions.NoSuchProtocolException;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.*;
import babel.events.consumers.ProtoConsumers;
import channel.ChannelEvent;
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
@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class GenericProtocol implements ProtoConsumers {

    private static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    //TODO split in GenericConnectionlessProtocol and GenericConnectionProtocol?

    private BlockingQueue<InternalEvent> queue;
    private Thread executionThread;
    private String protoName;
    private short protoId;

    private int defaultChannel;
    private Map<Integer, ChannelHandlers> channels;
    private Map<Short, TimerHandler<? extends ProtoTimer>> timerHandlers;
    private Map<Short, RequestHandler<? extends ProtoRequest>> requestHandlers;
    private Map<Short, ReplyHandler<? extends ProtoReply>> replyHandlers;
    private Map<Short, NotificationHandler<? extends ProtoNotification>> notificationHandlers;

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
        channels = new HashMap<>();
        defaultChannel = -1;

        //Initialize maps for event handlers
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
    public abstract void init(Properties props) throws HandlerRegistrationException;

    /**
     * Start the execution thread of the protocol
     */
    public final void start() {
        this.executionThread.start();
    }

    /* ------------------ PROTOCOL REGISTERS -------------------------------------------------*/

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
     * @param msgId     the numeric identifier of the message event
     * @param inHandler the function to process message event
     * @throws HandlerRegistrationException if a inHandler for the message id is already registered
     */
    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, null, null);
    }

    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageSentHandler<V> sentHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, sentHandler, null);
    }

    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageFailedHandler<V> failHandler)
            throws HandlerRegistrationException {
        registerMessageHandler(cId, msgId, inHandler, null, failHandler);
    }

    protected final <V extends ProtoMessage> void registerMessageHandler(int cId, short msgId,
                                                                         MessageInHandler<V> inHandler,
                                                                         MessageSentHandler<V> sentHandler,
                                                                         MessageFailedHandler<V> failHandler)
            throws HandlerRegistrationException {
        registerHandler(msgId, inHandler, getChannelOrThrow(cId).messageInHandlers);
        if (sentHandler != null) registerHandler(msgId, sentHandler, getChannelOrThrow(cId).messageSentHandlers);
        if (failHandler != null) registerHandler(msgId, failHandler, getChannelOrThrow(cId).messageFailedHandlers);
    }

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

    protected final void registerMessageSerializer(short msgId, ISerializer<ProtoMessage> serializer) {
        babel.registerSerializer(msgId, serializer);
    }

    protected final int createChannel(String channelName, Properties props) throws IOException {
        int channelId = babel.createChannel(channelName, this.protoId, this, props);
        registerSharedChannel(channelId);
        return channelId;
    }

    protected final void registerSharedChannel(int channelId){
        babel.registerChannelInterest(channelId, this.protoId, this);
        channels.put(channelId, new ChannelHandlers());
        if (defaultChannel == -1)
            setDefaultChannel(channelId);
    }

    protected final void setDefaultChannel(int channelId) {
        getChannelOrThrow(channelId);
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
        getChannelOrThrow(channelId);
        logger.debug("Sending: " + msg + " to " + destination + " proto " + destProto + " channel " + channelId);
        msg.destProto = destProto;
        msg.sourceProto = this.protoId;
        babel.sendMessage(channelId, msg, destination);
    }

    protected final void closeConnection(Host peer) {
        closeConnection(peer, defaultChannel);
    }

    protected final void closeConnection(Host peer, int channelId) {
        getChannelOrThrow(channelId);
        babel.closeConnection(channelId, peer);
    }

    /* ------------------ IPC BABEL PROXY -------------------------------------------------*/

    /**
     * Send a request to the destination protocol
     *
     * @param request request event
     * @throws NoSuchProtocolException if the protocol does not exists
     */
    protected final void sendRequest(ProtoRequest request, short destination) throws NoSuchProtocolException {
        babel.sendIPC(new IPCEvent(request, protoId, destination));
    }

    /**
     * Send a reply to the destination protocol
     *
     * @param reply reply event
     * @throws NoSuchProtocolException if the protocol does not exists
     */
    protected final void sendReply(ProtoReply reply, short destination) throws NoSuchProtocolException {
        babel.sendIPC(new IPCEvent(reply, protoId, destination));
    }

    // ------------------------------ NOTIFICATION BABEL PROXY ---------------------------------
    protected final void subscribeNotification(short nId, NotificationHandler<?> h)
            throws HandlerRegistrationException {
        registerHandler(nId, h, notificationHandlers);
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
     * Setups a t
     *
     * @param t       the t event
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
    @Override
    public final void deliverChannelEvent(CustomChannelEvent event) {
        queue.add(event);
    }

    @Override
    public final void deliverMessageIn(MessageInEvent msgIn) {
        queue.add(msgIn);
    }

    @Override
    public final void deliverMessageSent(MessageSentEvent event) {
        queue.add(event);
    }

    @Override
    public final void deliverMessageFailed(MessageFailedEvent event) {
        queue.add(event);
    }

    @Override
    public final void deliverTimer(TimerEvent timer) {
        queue.add(timer);
    }

    @Override
    public void deliverNotification(NotificationEvent notification) {
        queue.add(notification);
    }

    @Override
    public void deliverIPC(IPCEvent ipc) {
        queue.add(ipc);
    }

    /* ------------------ MAIN LOOP -------------------------------------------------*/
    private void mainLoop() {
        while (true) {
            try {
                InternalEvent pe = this.queue.take();
                logger.debug("Received: " + pe);
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
                        this.handleChannelEvent((CustomChannelEvent) pe);
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

    //TODO try catch (ClassCastException)
    private void handleMessageIn(MessageInEvent m) {
        ProtoMessage msg = m.getMsg();
        MessageInHandler h = getChannelOrThrow(m.getChannelId()).messageInHandlers.get(msg.getId());
        if (h != null)
            h.receive(msg, m.getFrom(), m.getMsg().sourceProto, m.getChannelId());
        else
            logger.warn("Discarding unexpected message (id " + m.getMsg().getId() + "): " + m);
    }

    private void handleMessageFailed(MessageFailedEvent e) {
        ProtoMessage msg = e.getMsg();
        MessageFailedHandler h = getChannelOrThrow(e.getChannelId()).messageFailedHandlers.get(msg.getId());
        if (h != null)
            h.onMessageFailed(msg, e.getTo(), e.getMsg().destProto, e.getCause(), e.getChannelId());
    }

    private void handleMessageSent(MessageSentEvent e) {
        ProtoMessage msg = e.getMsg();
        MessageSentHandler h = getChannelOrThrow(e.getChannelId()).messageSentHandlers.get(msg.getId());
        if (h != null)
            h.onMessageSent(msg, e.getTo(), e.getMsg().destProto, e.getChannelId());
    }

    private void handleChannelEvent(CustomChannelEvent m) {
        ChannelEventHandler h = getChannelOrThrow(m.getChannelId()).channelEventHandlers.get(m.getEvent().getId());
        if (h != null)
            h.handleEvent(m.getEvent(), m.getChannelId());
        else
            logger.warn("Discarding channel event (id " + m.getChannelId() + "): " + m);
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

    private void handleIPC(IPCEvent i) {
        switch (i.getIpc().getType()) {
            case REPLY:
                handleReply((ProtoReply) i.getIpc(), i.getSenderID());
                break;
            case REQUEST:
                handleRequest((ProtoRequest) i.getIpc(), i.getSenderID());
                break;
            default:
                throw new AssertionError("Ups");
        }
    }

    private static class ChannelHandlers {
        private Map<Short, MessageInHandler<? extends ProtoMessage>> messageInHandlers;
        private Map<Short, MessageSentHandler<? extends ProtoMessage>> messageSentHandlers;
        private Map<Short, MessageFailedHandler<? extends ProtoMessage>> messageFailedHandlers;
        private Map<Short, ChannelEventHandler<? extends ChannelEvent>> channelEventHandlers;

        public ChannelHandlers() {
            this.messageInHandlers = new HashMap<>();
            this.messageSentHandlers = new HashMap<>();
            this.messageFailedHandlers = new HashMap<>();
            this.channelEventHandlers = new HashMap<>();
        }
    }
}