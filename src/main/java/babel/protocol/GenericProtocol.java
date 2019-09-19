package babel.protocol;

import babel.Babel;
import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.exceptions.NotificationDoesNotExistException;
import babel.handlers.*;
import babel.requestreply.IReplyConsumer;
import babel.requestreply.IRequestConsumer;
import network.Host;
import network.IMessageConsumer;
import network.INetwork;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;
import babel.protocol.event.*;
import babel.requestreply.ProtocolReply;
import babel.requestreply.ProtocolRequest;
import babel.timer.ITimerConsumer;
import babel.timer.ProtocolTimer;
import network.ISerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class GenericProtocol implements IMessageConsumer, ITimerConsumer, INotificationConsumer, IRequestConsumer, IReplyConsumer, INotificationProducer, Runnable {

    private BlockingQueue<ProtocolEvent> queue;
    private Thread executionThread;
    private String protoName;
    private short protoId;
    protected final INetwork network;

    private Map<Short, ProtocolMessageHandler> messageHandlers;
    private Map<Short, ProtocolTimerHandler> timerHandlers;
    private Map<Short, ProtocolRequestHandler> requestHandlers;
    private Map<Short, ProtocolReplyHandler> replyHandlers;
    private Map<Short, ProtocolNotificationHandler> notificationHandlers;

    private Map<String, Short> producedNotifications;
    private Map<Short, String> producedNotificationsById;

    private Map<Short, Set<INotificationConsumer>> subscribers;

    public static final Logger logger = LogManager.getLogger(GenericProtocol.class);

    private static Babel babel = Babel.getInstance();

    public GenericProtocol(String protoName, short protoId, INetwork net) {
        this.queue = new LinkedBlockingQueue<>();
        this.protoId = protoId;
        this.protoName = protoName;
        this.network = net;
        this.executionThread = new Thread(this, "Protocol " + protoId + " (" + protoName + ")");

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

    public final short getProtoId() {
        return protoId;
    }

    public final String getProtoName() {
        return protoName;
    }

    public abstract void init(Properties props);

    public final void start() {
        this.executionThread.start();
    }

    protected final void registerMessageHandler(short id, ProtocolMessageHandler h, ISerializer<? extends ProtocolMessage> serializer) throws HandlerRegistrationException {
        if(this.messageHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for message with id " + id + ".");
        network.registerConsumer(id, this);
        network.registerSerializer(id, serializer);
        this.messageHandlers.put(id, h);
    }

    protected final void registerTimerHandler(short id, ProtocolTimerHandler h) throws HandlerRegistrationException {
        if(this.timerHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for timer with id " + id + ".");
        this.timerHandlers.put(id, h);
    }

    protected final void registerRequestHandler(short id, ProtocolRequestHandler h) throws HandlerRegistrationException {
        if(this.requestHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for request with id " + id + ".");
        this.requestHandlers.put(id, h);
    }

    protected final void registerReplyHandler(short id, ProtocolReplyHandler h) throws HandlerRegistrationException {
        if(this.replyHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for reply with id " + id + ".");
        this.replyHandlers.put(id, h);
    }

    protected final void registerNotificationHandler(short id, ProtocolNotificationHandler h) throws HandlerRegistrationException {
        if(this.notificationHandlers.containsKey(id))
            throw new HandlerRegistrationException("Conflict in registering handler for notification with id " + id + ".");
        this.notificationHandlers.put(id, h);
    }

    @Override
    public final void run() {
        while(true) {
            try {
                ProtocolEvent pe = this.queue.take();
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

    //Interface to send messages
    protected final void sendMessage(ProtocolMessage msg, Host destination) {
        network.sendMessage(msg.getId(), msg, destination);
    }

    //Interface to send requests
    protected final void sendRequest(ProtocolRequest r) throws DestinationProtocolDoesNotExist {
        r.setSender(this.getProtoId());
        GenericProtocol gp = babel.getProtocol(r.getDestinationID());
        if(gp == null)
            throw new DestinationProtocolDoesNotExist("Destination of Request invalid (proto: " + r.getDestinationID() + ")" );
        gp.deliverRequest(r);
    }

    //Interface to send replies
    protected final void sendReply(ProtocolReply r) throws DestinationProtocolDoesNotExist  {
        r.setSender(this.getProtoId());
        GenericProtocol gp = babel.getProtocol(r.getDestinationID());
        if(gp == null)
            throw new DestinationProtocolDoesNotExist("Destination of Reply invalid (proto: " + r.getDestinationID() + ")" );
        gp.deliverReply(r);
    }

    //Interface to manage Timers
    public UUID createPeriodicTimer(ProtocolTimer t, long first, long period) {
        return babel.createPeriodicTimer(this, t, first, period);
    }

    public UUID createTimer(ProtocolTimer t, long timeout) {
        return babel.createTimer(this, t, timeout);
    }

    public ProtocolTimer cancelTimer(UUID timerID) {
        return babel.cancelTimer(timerID);
    }

    @Override
    public final void deliverMessage(short msgCode, Object msg, Host from) {
        ProtocolMessage pm = (ProtocolMessage) msg;
        pm.setId(msgCode);
        pm.setFrom(from);
        queue.add(pm);
    }

    @Override
    public final void deliverTimer(ProtocolTimer timer) {
        queue.add(timer);
    }

    @Override
    public void deliverNotification(ProtocolNotification notification) {
        queue.add(notification);
    }

    @Override
    public void deliverRequest(ProtocolRequest r) { queue.add(r); }

    @Override
    public void deliverReply(ProtocolReply r) { queue.add(r); }

    //Methods from INotificationProducer
    @Override
    public final void subscribeNotification(short notificationID, INotificationConsumer c) throws NotificationDoesNotExistException {
        if(!this.producedNotificationsById.containsKey(notificationID))
            throw new NotificationDoesNotExistException("Protocol " + this.getProtoName() + " does not produce babel.notification with id: " + notificationID);
        this.subscribers.computeIfAbsent(notificationID, k -> new HashSet<>()).add(c);
    }

    @Override
    public final void unsubscribeNotification(short notificationID, INotificationConsumer c) {
        this.subscribers.getOrDefault(notificationID, Collections.EMPTY_SET).remove(c);
    }

    @Override
    public void subscribeNotification(String notification, INotificationConsumer c) throws NotificationDoesNotExistException {
        Short s = this.producedNotifications.get(notification);
        if(s==null)
            throw new NotificationDoesNotExistException("Protocol " + this.getProtoName() + " does not produce babel.notification with name: " + notification);
        this.subscribeNotification(s,c);
    }

    @Override
    public void unsubscribeNotification(String notification, INotificationConsumer c) {
        Short s = this.producedNotifications.get(notification);
        if(s != null)
            this.subscribers.getOrDefault(s, Collections.EMPTY_SET).remove(c);
    }

    protected final void registerNotification(short id, String name) {
        this.producedNotifications.put(name, id);
        this.producedNotificationsById.put(id, name);
    }

    @Override
    public Map<String, Short> producedNotifications() {
        return this.producedNotifications;
    }

    protected final void triggerNotification(ProtocolNotification n) {
        n.setEmitter(this.getProtoId());
        if(this.subscribers.containsKey(n.getId())) {
            for (INotificationConsumer c : this.subscribers.get(n.getId())) {
                c.deliverNotification(n);
            }
        }
    }
}