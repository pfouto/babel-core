package babel;

import babel.exceptions.InvalidParameterException;
import babel.exceptions.ProtocolAlreadyExistsException;
import babel.protocol.GenericProtocol;
import babel.timer.ITimerConsumer;
import babel.timer.ProtocolTimer;
import network.INetwork;
import network.NetworkService;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


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
 *</pre>
 *
 * For more information on protocol implementation with Babel:
 * @see babel.protocol.GenericProtocol
 */
public class Babel {

    private static Babel system;

    private Map<Short, GenericProtocol> protocolMap;
    private Map<String, GenericProtocol> protocolByNameMap;

    private Map<UUID,QueuedTimer> allTimers;
    private PriorityBlockingQueue<QueuedTimer> timerQueue;
    private Thread worker;

    private INetwork network;

    private Properties configuration;

    /**
     * Returns the instance of the Babel Runtime
     * @return the Babel instance
     */
    public static synchronized Babel getInstance() {
        if(system == null)
            system = new Babel();
        return system;
    }

    private Babel() {
        this.protocolMap = new HashMap<>();
        this.protocolByNameMap = new HashMap<>();
        allTimers = new HashMap<>();
        timerQueue = new PriorityBlockingQueue<>();
        worker = new Thread(() -> {
            while(true){
                long now = System.currentTimeMillis();
                QueuedTimer qT = timerQueue.peek();
                long toSleep;
                if(qT != null)
                    toSleep = qT.triggerTime - now;
                else
                    toSleep = Long.MAX_VALUE;

                if(toSleep <= 0){
                    QueuedTimer t = timerQueue.poll();
                    //Deliver
                    t.getProtocol().deliverTimer((ProtocolTimer) t.getEvent().clone());
                    if(t.isPeriodic()) {
                        t.setTriggerTime(now + t.getPeriod());
                        timerQueue.add(t);
                    }
                } else {
                    try {
                        Thread.sleep(toSleep);
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        });
    }

    /**
     * Register a protocol in Babel
     * @param protocol the protocol to registered
     * @throws ProtocolAlreadyExistsException if a protocol with the same id or name has already been registered in Babel
     */
    public void registerProtocol(GenericProtocol protocol) throws ProtocolAlreadyExistsException {
        if(protocolMap.containsKey(protocol.getProtoId()))
            throw new ProtocolAlreadyExistsException("Protocol conflicts on id with protocol: id=" + protocol.getProtoId() + ":name=" + protocolMap.get(protocol.getProtoId()).getProtoName());
        if(protocolByNameMap.containsKey(protocol.getProtoName()))
            throw new ProtocolAlreadyExistsException("Protocol conflicts on name: " + protocol.getProtoName() + " (id: " + this.protocolByNameMap.get(protocol.getProtoName()).getProtoId()+ ")");

        this.protocolMap.put(protocol.getProtoId(), protocol);
        this.protocolByNameMap.put(protocol.getProtoName(), protocol);
    }

    /**
     * Returns the protocol registered in Babel with the name provided
     * @param name the name of the protocol
     * @return the protocol or null if it is not registered
     */
    public GenericProtocol getProtocolByName(String name) {
        GenericProtocol gp = this.protocolByNameMap.get(name);
        return gp;
    }

    /**
     * Returns the protocol registered in Babel with the id provided
     * @param id the numeric identifier of the protocol
     * @return the protocol or null if it is not registered
     */
    public GenericProtocol getProtocol(short id) {
        GenericProtocol gp = protocolMap.get(id);
        return gp;
    }

    /**
     * Returns the name of the protocol register in Babel with the id provided
     * @param id the numeric identifier of the protocol
     * @return the protocol name or null if the protocol is not registered
     */
    public String getProtocolName(short id) {
        GenericProtocol gp = protocolMap.get(id);
        if(gp != null)
            return gp.getProtoName();
        else
            return null;
    }

    /**
     * Begins the execution of all protocols registered in Babel
     */
    public void start(){
        worker.start();
        for(GenericProtocol gp: protocolMap.values())
            gp.start();
    }


    /**
     * Setups a periodic timer to be monitored by Babel
     * @param consumerProtocol the protocol that setup the periodic timer
     * @param timer the timer event to be monitored
     * @param firstNotification the amount of time until the first trigger of the timer event
     * @param period the periodicity of the timer event
     * @return the unique id of the timer event setup
     */
    public UUID setupPeriodicTimer(ITimerConsumer consumerProtocol, ProtocolTimer timer, long firstNotification, long period) {
        if(allTimers.containsKey(timer.getUuid()))
            return null;
        timerQueue.add(new QueuedTimer(System.currentTimeMillis() + firstNotification, period, true, timer, consumerProtocol));
        worker.interrupt();
        return timer.getUuid();
    }

    /**
     * Setups a timer to be monitored by Babel
     * @param consumerProtocol the protocol that setup the timer
     * @param timer the timer event to be monitored
     * @param timeout the amount of time until the timer event is triggered
     * @return the unique id of the timer event setup
     */
    public UUID setupTimer(ITimerConsumer consumerProtocol, ProtocolTimer timer, long timeout) {
        if(allTimers.containsKey(timer.getUuid()))
            return null;
        timerQueue.add(new QueuedTimer(System.currentTimeMillis() + timeout, -1, false, timer, consumerProtocol));
        worker.interrupt();
        return timer.getUuid();
    }

    /**
     * Cancels a timer that was being monitored by Babel
     * Babel will forget that the timer exists
     * @param timerID the unique id of the timer event to be canceled
     * @return the timer event or null if it was not being monitored by Babel
     */
    public ProtocolTimer cancelTimer(UUID timerID) {
        if(!allTimers.containsKey(timerID)) {
            return null;
        }
        timerQueue.remove(allTimers.get(timerID));
        ProtocolTimer t = allTimers.remove(timerID).getEvent();
        worker.interrupt();
        return t;
    }

    /**
     * Reads the provided properties files and builds a configuration
     * Console parameters override or add properties in the provided file
     *
     * properties should be provided as:   propertyName=value
     *
     * @param propsFilename the path to the properties file
     * @param args console parameters
     * @return the configurations built
     * @throws IOException if the provided file does not exist
     * @throws InvalidParameterException if the console parameters are not in the format: prop=value
     */
    public Properties loadConfig(String propsFilename, String[] args) throws IOException, InvalidParameterException {
        configuration = new Properties();
        configuration.load(new FileInputStream(propsFilename));
        //Override with launch parameter props
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String[] property = arg.split("=");
            if (property.length == 2)
                configuration.setProperty(property[0], property[1]);
            else
                throw new InvalidParameterException("Unknown parameter: " + arg);
        }
        return this.configuration;
    }

    /**
     * Returns the instance of the network service
     * @return the network service instance
     * @throws Exception if no configuration was previously set
     */
    public synchronized INetwork getNetworkInstance() throws Exception {
        if(this.network == null) {
            if(this.configuration == null)
                throw new Exception("Cannot access network without loading configuration.");
            this.network = new NetworkService(this.configuration);
        }
        return this.network;
    }

    private static class QueuedTimer implements Comparable<QueuedTimer>, Comparator<QueuedTimer> {
        private final ITimerConsumer protocol;
        private long triggerTime;
        private final long period;
        private final boolean periodic;
        private final ProtocolTimer event;

        private QueuedTimer(long triggerTime, long period, boolean periodic, ProtocolTimer event, ITimerConsumer consumer){
            this.triggerTime = triggerTime;
            this.period = period;
            this.periodic = periodic;
            this.event = event;
            this.protocol = consumer;
        }

        public ITimerConsumer getProtocol() {
            return protocol;
        }

        long getPeriod() {
            return period;
        }

        /**long getTriggerTime() {
            return triggerTime;
        }**/

        void setTriggerTime(long triggerTime) {
            this.triggerTime = triggerTime;
        }

        public ProtocolTimer getEvent() {
            return event;
        }

        boolean isPeriodic() {
            return periodic;
        }

        @Override
        public int compareTo(QueuedTimer o) {
            return Long.compare(this.triggerTime, o.triggerTime);
        }

        @Override
        public int compare(QueuedTimer o1, QueuedTimer o2) {
            return Long.compare(o1.triggerTime, o2.triggerTime);
        }
    }
}
