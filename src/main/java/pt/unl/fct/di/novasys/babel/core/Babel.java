package pt.unl.fct.di.novasys.babel.core;

import pt.unl.fct.di.novasys.babel.internal.BabelMessage;
import pt.unl.fct.di.novasys.babel.internal.IPCEvent;
import pt.unl.fct.di.novasys.babel.internal.NotificationEvent;
import pt.unl.fct.di.novasys.babel.internal.TimerEvent;
import pt.unl.fct.di.novasys.babel.exceptions.InvalidParameterException;
import pt.unl.fct.di.novasys.babel.exceptions.NoSuchProtocolException;
import pt.unl.fct.di.novasys.babel.exceptions.ProtocolAlreadyExistsException;
import pt.unl.fct.di.novasys.babel.metrics.MetricsManager;
import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;
import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;
import pt.unl.fct.di.novasys.babel.initializers.ChannelInitializer;
import pt.unl.fct.di.novasys.babel.initializers.SimpleClientChannelInitializer;
import pt.unl.fct.di.novasys.babel.initializers.SimpleServerChannelInitializer;
import pt.unl.fct.di.novasys.babel.initializers.TCPChannelInitializer;
import pt.unl.fct.di.novasys.channel.IChannel;
import pt.unl.fct.di.novasys.channel.simpleclientserver.SimpleClientChannel;
import pt.unl.fct.di.novasys.channel.simpleclientserver.SimpleServerChannel;
import pt.unl.fct.di.novasys.channel.tcp.TCPChannel;
import pt.unl.fct.di.novasys.network.ISerializer;
import pt.unl.fct.di.novasys.network.data.Host;
import org.apache.commons.lang3.tuple.Triple;

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
    private final Map<Short, GenericProtocol> protocolMap;
    private final Map<String, GenericProtocol> protocolByNameMap;
    private final Map<Short, Set<GenericProtocol>> subscribers;

    //Timers
    private final Map<Long, TimerEvent> allTimers;
    private final PriorityBlockingQueue<TimerEvent> timerQueue;
    private final Thread timersThread;
    private final AtomicLong timersCounter;

    //Channels
    private final Map<String, ChannelInitializer<? extends IChannel<BabelMessage>>> initializers;

    private final Map<Integer,
            Triple<IChannel<BabelMessage>, ChannelToProtoForwarder, BabelMessageSerializer>> channelMap;
    private final AtomicInteger channelIdGenerator;

    private long startTime;
    private boolean started = false;

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
        this.initializers = new ConcurrentHashMap<>();

        registerChannelInitializer(SimpleClientChannel.NAME, new SimpleClientChannelInitializer());
        registerChannelInitializer(SimpleServerChannel.NAME, new SimpleServerChannelInitializer());
        registerChannelInitializer(TCPChannel.NAME, new TCPChannelInitializer());

        //registerChannelInitializer("Ackos", new AckosChannelInitializer());
        //registerChannelInitializer(MultithreadedTCPChannel.NAME, new MultithreadedTCPChannelInitializer());
    }

    private void timerLoop() {
        while (true) {
            long now = getMillisSinceStart();
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
        startTime = System.currentTimeMillis();
        started = true;
        MetricsManager.getInstance().start();
        timersThread.start();
        protocolMap.values().forEach(GenericProtocol::start);
    }

    /**
     * Register a protocol in Babel
     *
     * @param p the protocol to registered
     * @throws ProtocolAlreadyExistsException if a protocol with the same id or name has already been registered
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

    /**
     * Registers a new channel in babel
     *
     * @param name        the channel name
     * @param initializer the channel initializer
     */
    public void registerChannelInitializer(String name,
                                           ChannelInitializer<? extends IChannel<BabelMessage>> initializer) {
        ChannelInitializer<? extends IChannel<BabelMessage>> old = initializers.putIfAbsent(name, initializer);
        if (old != null) {
            throw new IllegalArgumentException("Initializer for channel with name " + name +
                    " already registered: " + old);
        }
    }

    /**
     * Creates a channel for a protocol
     * Called by {@link GenericProtocol}. Do not evoke directly.
     *
     * @param channelName the name of the channel to create
     * @param protoId     the protocol numeric identifier
     * @param props       the properties required by the channel
     * @return the channel Id
     * @throws IOException if channel creation fails
     */
    int createChannel(String channelName, short protoId, Properties props)
            throws IOException {
        ChannelInitializer<? extends IChannel<?>> initializer = initializers.get(channelName);
        if (initializer == null)
            throw new IllegalArgumentException("Channel initializer not registered: " + channelName);

        int channelId = channelIdGenerator.incrementAndGet();
        BabelMessageSerializer serializer = new BabelMessageSerializer(new ConcurrentHashMap<>());
        ChannelToProtoForwarder forwarder = new ChannelToProtoForwarder(channelId);
        IChannel<BabelMessage> newChannel = initializer.initialize(serializer, forwarder, props, protoId);
        channelMap.put(channelId, Triple.of(newChannel, forwarder, serializer));
        return channelId;
    }

    /**
     * Registers interest in receiving events from a channel.
     *
     * @param consumerProto the protocol that will receive events generated by the new channel
     *                      Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void registerChannelInterest(int channelId, short protoId, GenericProtocol consumerProto) {
        ChannelToProtoForwarder forwarder = channelMap.get(channelId).getMiddle();
        forwarder.addConsumer(protoId, consumerProto);
    }

    /**
     * Sends a message to a peer using the given channel and connection.
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void sendMessage(int channelId, int connection, BabelMessage msg, Host target) {
        Triple<IChannel<BabelMessage>, ChannelToProtoForwarder, BabelMessageSerializer> channelEntry =
                channelMap.get(channelId);
        if (channelEntry == null)
            throw new AssertionError("Sending message to non-existing channelId " + channelId);
        channelEntry.getLeft().sendMessage(msg, target, connection);
    }

    /**
     * Closes a connection to a peer in a given channel.
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void closeConnection(int channelId, Host target, int connection) {
        Triple<IChannel<BabelMessage>, ChannelToProtoForwarder, BabelMessageSerializer> channelEntry =
                channelMap.get(channelId);
        if (channelEntry == null)
            throw new AssertionError("Closing connection in non-existing channelId " + channelId);
        channelEntry.getLeft().closeConnection(target, connection);
    }

    /**
     * Opens a connection to a peer in the given channel.
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void openConnection(int channelId, Host target) {
        Triple<IChannel<BabelMessage>, ChannelToProtoForwarder, BabelMessageSerializer> channelEntry =
                channelMap.get(channelId);
        if (channelEntry == null)
            throw new AssertionError("Opening connection in non-existing channelId " + channelId);
        channelEntry.getLeft().openConnection(target);
    }

    /**
     * Registers a (de)serializer for a message type.
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void registerSerializer(int channelId, short msgCode, ISerializer<? extends ProtoMessage> serializer) {
        Triple<IChannel<BabelMessage>, ChannelToProtoForwarder, BabelMessageSerializer> channelEntry =
                channelMap.get(channelId);
        if (channelEntry == null)
            throw new AssertionError("Registering serializer in non-existing channelId " + channelId);
        channelEntry.getRight().registerProtoSerializer(msgCode, serializer);
    }

    // ----------------------------- REQUEST / REPLY / NOTIFY

    /**
     * Send a request/reply to a protocol
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void sendIPC(IPCEvent ipc) throws NoSuchProtocolException {
        GenericProtocol gp = protocolMap.get(ipc.getDestinationID());
        if (gp == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(ipc.getDestinationID()).append(" not executing.");
            sb.append("Executing protocols: [");
            protocolMap.forEach((id, p) -> sb.append(id).append(" - ").append(p.getProtoName()).append(", "));
            sb.append("]");
            throw new NoSuchProtocolException(sb.toString());
        }
        gp.deliverIPC(ipc);
    }

    /**
     * Subscribes a protocol to a notification
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void subscribeNotification(short nId, GenericProtocol consumer) {
        subscribers.computeIfAbsent(nId, k -> ConcurrentHashMap.newKeySet()).add(consumer);
    }

    /**
     * Unsubscribes a protocol from a notification
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void unsubscribeNotification(short nId, GenericProtocol consumer) {
        subscribers.getOrDefault(nId, Collections.emptySet()).remove(consumer);
    }

    /**
     * Triggers a notification, delivering to all subscribed protocols
     * Called by {@link GenericProtocol}. Do not evoke directly.
     */
    void triggerNotification(NotificationEvent n) {
        for (GenericProtocol c : subscribers.getOrDefault(n.getNotification().getId(), Collections.emptySet())) {
            c.deliverNotification(n);
        }
    }
    // ---------------------------- TIMERS

    /**
     * Setups a periodic timer to be monitored by Babel
     * Called by {@link GenericProtocol}. Do not evoke directly.
     *
     * @param consumer the protocol that setup the periodic timer
     * @param first    the amount of time until the first trigger of the timer event
     * @param period   the periodicity of the timer event
     */
    long setupPeriodicTimer(ProtoTimer t, GenericProtocol consumer, long first, long period) {
        long id = timersCounter.incrementAndGet();
        TimerEvent newTimer = new TimerEvent(t, id, consumer,
                getMillisSinceStart() + first, true, period);
        allTimers.put(newTimer.getUuid(), newTimer);
        timerQueue.add(newTimer);
        timersThread.interrupt();
        return id;
    }

    /**
     * Setups a timer to be monitored by Babel
     * Called by {@link GenericProtocol}. Do not evoke directly.
     *
     * @param consumer the protocol that setup the timer
     * @param timeout  the amount of time until the timer event is triggered
     */
    long setupTimer(ProtoTimer t, GenericProtocol consumer, long timeout) {
        long id = timersCounter.incrementAndGet();
        TimerEvent newTimer = new TimerEvent(t, id, consumer,
                getMillisSinceStart() + timeout, false, -1);
        timerQueue.add(newTimer);
        allTimers.put(newTimer.getUuid(), newTimer);
        timersThread.interrupt();
        return id;
    }

    /**
     * Cancels a timer that was being monitored by Babel
     * Babel will forget that the timer exists
     * Called by {@link GenericProtocol}. Do not evoke directly.
     *
     * @param timerID the unique id of the timer event to be canceled
     * @return the timer event or null if it was not being monitored by Babel
     */
    ProtoTimer cancelTimer(long timerID) {
        TimerEvent tE = allTimers.remove(timerID);
        if (tE == null)
            return null;
        timerQueue.remove(tE);
        timersThread.interrupt(); //TODO is this needed?
        return tE.getTimer();
    }

    // ---------------------------- CONFIG

    /**
     * Reads either the default or the given properties file (the file can be given with the argument -config)
     * Builds a configuration file with the properties from the file and then merges ad-hoc properties given
     * in the arguments.
     * <p>
     * Argument properties should be provided as:   propertyName=value
     *
     * @param defaultConfigFile the path to the default properties file
     * @param args              console parameters
     * @return the configurations built
     * @throws IOException               if the provided file does not exist
     * @throws InvalidParameterException if the console parameters are not in the format: prop=value
     */
    public static Properties loadConfig(String[] args, String defaultConfigFile)
            throws IOException, InvalidParameterException {

        List<String> argsList = new ArrayList<>();
        Collections.addAll(argsList, args);
        String configFile = extractConfigFileFromArguments(argsList, defaultConfigFile);

        Properties configuration = new Properties();
        if (configFile != null)
            configuration.load(new FileInputStream(configFile));
        //Override with launch parameter props
        for (String arg : argsList) {
            String[] property = arg.split("=");
            if (property.length == 2)
                configuration.setProperty(property[0], property[1]);
            else
                throw new InvalidParameterException("Unknown parameter: " + arg);
        }
        return configuration;
    }

    private static String extractConfigFileFromArguments(List<String> args, String defaultConfigFile) {
        String config = defaultConfigFile;
        Iterator<String> iter = args.iterator();
        while (iter.hasNext()) {
            String param = iter.next();
            if (param.equals("-conf")) {
                if (iter.hasNext()) {
                    iter.remove();
                    config = iter.next();
                    iter.remove();
                }
                break;
            }
        }
        return config;
    }

    public long getMillisSinceStart() {
        return started ? System.currentTimeMillis() - startTime : 0;
    }

}
