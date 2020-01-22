package babel.runtime;

import babel.runtime.events.*;
import babel.runtime.exceptions.NoSuchProtocolException;
import babel.runtime.exceptions.InvalidParameterException;
import babel.runtime.exceptions.ProtocolAlreadyExistsException;
import babel.genericprotocol.GenericProtocol;
import babel.runtime.initializers.AckosChannelInitializer;
import babel.runtime.initializers.ChannelInitializer;
import babel.runtime.initializers.ClientChannelInitializer;
import babel.runtime.initializers.ServerChannelInitializer;
import babel.runtime.protocol.ProtoMessage;
import babel.runtime.protocol.ProtoTimer;
import babel.runtime.events.consumers.ChannelConsumer;
import babel.runtime.events.consumers.NotificationConsumer;
import babel.runtime.events.consumers.TimerConsumer;
import channel.IChannel;
import network.ISerializer;
import network.data.Host;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Babel class provides applications with a Runtime that supports
 * the execution of protocols.
 *
 * <p> An example of how to use the class follows:
 *
 * <pre>
 *         Babel babel = Babel.getInstance(); //initialize babel
 *         Properties configProps = babel.loadConfig("network_config.properties", args);
 *         INetwork net = babel.getNetworkInstance();
 *
 *         //Define protocols
 *         ProtocolA protoA = new ProtocolA(net);
 *         protoA.init(configProps);
 *
 *         ProtocolB protoB = new ProtocolB(net);
 *         protoB.init(configProps);
 *
 *         //Register protocols
 *         babel.registerProtocol(protoA);
 *         babel.registerProtocol(protoB);
 *
 *         //subscribe to notifications
 *         protoA.subscribeNotification(protoA.NOTIFICATION_ID, this);
 *
 *         //start babel runtime
 *         babel.start();
 *
 *         //Application Logic
 *
 * </pre>
 * <p>
 * For more information on protocol implementation with Babel:
 *
 * @see GenericProtocol
 */
public class Babel {

    private static Babel system;

    /**
     * Returns the instance of the Babel Runtime
     *
     * @return the Babel instance
     */
    public static synchronized Babel getInstance() {
        if (system == null)
            system = new Babel();
        return system;
    }

    //Protocols
    private Map<Short, GenericProtocol> protocolMap;
    private Map<String, GenericProtocol> protocolByNameMap;
    private Map<Short, Set<NotificationConsumer>> subscribers;

    //Timers
    private Map<Long, TimerEvent> allTimers;
    private PriorityBlockingQueue<TimerEvent> timerQueue;
    private Thread timersThread;
    private AtomicLong timersCounter;

    //Channels
    private Map<String, ChannelInitializer<? extends IChannel<AddressedMessage>>> initializers;
    private AddressedMessageSerializer msgSerializer;
    private Map<Integer, Pair<IChannel<AddressedMessage>, ChannelToProtoForwarder>> channelMap;
    private AtomicInteger channelIdGenerator;

    private Babel() {
        //Protocols
        this.protocolMap = new ConcurrentHashMap<>();
        this.protocolByNameMap = new ConcurrentHashMap<>();
        this.subscribers = new ConcurrentHashMap<>();

        //Timers
        allTimers = new HashMap<>();
        timerQueue = new PriorityBlockingQueue<>();
        timersCounter = new AtomicLong();
        timersThread = new Thread(this::timerLoop);

        //Channels
        channelMap = new ConcurrentHashMap<>();
        channelIdGenerator = new AtomicInteger(0);
        msgSerializer = new AddressedMessageSerializer(new ConcurrentHashMap<>());
        this.initializers = new ConcurrentHashMap<>();
        initializers.put("Ackos", new AckosChannelInitializer());
        initializers.put("SimpleClient", new ClientChannelInitializer());
        initializers.put("SimpleServer", new ServerChannelInitializer());
    }

    private void timerLoop() {
        while (true) {
            long now = System.currentTimeMillis();
            TimerEvent tE = timerQueue.peek();

            long toSleep = tE != null ? tE.getTriggerTime() - now : Long.MAX_VALUE;

            if (toSleep <= 0) {
                TimerEvent t = timerQueue.remove();
                //Deliver
                t.getConsumer().deliverTimer(t);
                if (t.isPeriodic()) {
                    t.setTriggerTime(now + t.getPeriod());
                    timerQueue.add(t);
                }
            } else {
                try {
                    Thread.sleep(toSleep);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    /**
     * Begins the execution of all protocols registered in Babel
     */
    public void start() {
        timersThread.start();
        protocolMap.values().forEach(GenericProtocol::start);
    }

    /**
     * Register a protocol in Babel
     *
     * @param p the protocol to registered
     * @throws ProtocolAlreadyExistsException if a protocol with the same id or name has already been registered in Babel
     */
    public void registerProtocol(GenericProtocol p) throws ProtocolAlreadyExistsException {
        GenericProtocol old = protocolMap.putIfAbsent(p.getProtoId(), p);
        if (old != null) throw new ProtocolAlreadyExistsException(
                "Protocol conflicts on id with protocol: id=" + p.getProtoId() + ":name=" + protocolMap.get(
                        p.getProtoId()).getProtoName());
        old = protocolByNameMap.putIfAbsent(p.getProtoName(), p);
        if (old != null) {
            protocolMap.remove(p.getProtoId());
            throw new ProtocolAlreadyExistsException(
                    "Protocol conflicts on name: " + p.getProtoName() + " (id: " + this.protocolByNameMap.get(
                            p.getProtoName()).getProtoId() + ")");
        }
    }

    // ----------------------------- NETWORK
    public int createChannel(String channelName, short protoId, ChannelConsumer consumerProto, Properties props)
            throws IOException {
        ChannelInitializer<? extends IChannel<?>> initializer = initializers.get(channelName);
        if (initializer == null)
            throw new IllegalArgumentException("Channel initializer not registered: " + channelName);

        int channelId = channelIdGenerator.incrementAndGet();
        ChannelToProtoForwarder forwarder = new ChannelToProtoForwarder(channelId);
        IChannel<AddressedMessage> newChannel = initializer.initialize(msgSerializer, forwarder, props);
        channelMap.put(channelId, Pair.of(newChannel, forwarder));
        return channelId;
    }

    public void registerChannelInterest(int channelId, short protoId, ChannelConsumer consumerProto){
        ChannelToProtoForwarder forwarder = channelMap.get(channelId).getValue();
        forwarder.addConsumer(protoId, consumerProto);
    }

    public int getSharedChannel(){
        //TODO share channels somehow
        throw new NotImplementedException("Not implemented...");
    }

    public void sendMessage(int channelId, AddressedMessage msg, Host target){
        Pair<IChannel<AddressedMessage>, ChannelToProtoForwarder> channelPair = channelMap.get(channelId);
        if(channelPair == null)
            throw new AssertionError("Sending message to non-existing channelId " + channelId);
        channelPair.getKey().sendMessage(msg, target);
    }

    public void closeConnection(int channelId, Host target){
        Pair<IChannel<AddressedMessage>, ChannelToProtoForwarder> channelPair = channelMap.get(channelId);
        if(channelPair == null)
            throw new AssertionError("Closing connection in non-existing channelId " + channelId);
        channelPair.getKey().closeConnection(target);
    }

    public void registerSerializer(short msgCode, ISerializer<ProtoMessage> serializer) {
        msgSerializer.registerProtoSerializer(msgCode, serializer);
    }

    // ----------------------------- REQUEST / REPLY / NOTIFY
    public void sendIPC(IPCEvent ipc) throws NoSuchProtocolException {
        GenericProtocol gp = protocolMap.get(ipc.getDestinationID());
        if (gp == null)throw new NoSuchProtocolException(ipc.getDestinationID());
        gp.deliverIPC(ipc);
    }

    public void subscribeNotification(short nId, NotificationConsumer consumer) {
        subscribers.computeIfAbsent(nId, k -> ConcurrentHashMap.newKeySet()).add(consumer);
    }

    public void unsubscribeNotification(short nId, NotificationConsumer consumer) {
        subscribers.getOrDefault(nId, Collections.emptySet()).remove(consumer);
    }

    public void triggerNotification(NotificationEvent n) {
        for (NotificationConsumer c : subscribers.getOrDefault(n.getNotification().getId(), Collections.emptySet())) {
            c.deliverNotification(n);
        }
    }
    // ---------------------------- TIMERS

    /**
     * Setups a periodic timer to be monitored by Babel
     *
     * @param consumer the protocol that setup the periodic timer
     * @param first    the amount of time until the first trigger of the timer event
     * @param period   the periodicity of the timer event
     */
    public long setupPeriodicTimer(ProtoTimer t, TimerConsumer consumer, long first, long period) {
        long id = timersCounter.incrementAndGet();
        timerQueue.add(new TimerEvent(t, id, consumer,
                System.currentTimeMillis() + first, true, period));
        timersThread.interrupt();
        return id;
    }

    /**
     * Setups a timer to be monitored by Babel
     *
     * @param consumer the protocol that setup the timer
     * @param timeout  the amount of time until the timer event is triggered
     */
    public long setupTimer(ProtoTimer t, TimerConsumer consumer, long timeout) {
        long id = timersCounter.incrementAndGet();
        TimerEvent newTimer = new TimerEvent(t, id, consumer,
                System.currentTimeMillis() + timeout, false, -1);
        timerQueue.add(newTimer);
        allTimers.put(newTimer.getUuid(), newTimer);
        timersThread.interrupt();
        return id;
    }

    /**
     * Cancels a timer that was being monitored by Babel
     * Babel will forget that the timer exists
     *
     * @param timerID the unique id of the timer event to be canceled
     * @return the timer event or null if it was not being monitored by Babel
     */
    public ProtoTimer cancelTimer(long timerID) {
        TimerEvent tE = allTimers.remove(timerID);
        if (tE == null)
            return null;
        timerQueue.remove(tE);
        timersThread.interrupt(); //TODO is this needed?
        return tE.getTimer();
    }

    // ---------------------------- CONFIG

    /**
     * Reads the provided properties files and builds a configuration
     * Console parameters override or add properties in the provided file
     * <p>
     * properties should be provided as:   propertyName=value
     *
     * @param propsFilename the path to the properties file
     * @param args          console parameters
     * @return the configurations built
     * @throws IOException               if the provided file does not exist
     * @throws InvalidParameterException if the console parameters are not in the format: prop=value
     */
    public Properties loadConfig(String propsFilename, String[] args)
            throws IOException, InvalidParameterException {
        Properties configuration = new Properties();
        configuration.load(new FileInputStream(propsFilename));
        //Override with launch parameter props
        for (String arg : args) {
            String[] property = arg.split("=");
            if (property.length == 2)
                configuration.setProperty(property[0], property[1]);
            else
                throw new InvalidParameterException("Unknown parameter: " + arg);
        }
        return configuration;
    }
}
