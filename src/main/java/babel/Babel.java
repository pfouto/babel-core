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

public class Babel {

    private static Babel system;

    private Map<Short, GenericProtocol> protocolMap;
    private Map<String, GenericProtocol> protocolByNameMap;

    private Map<UUID,QueuedTimer> allTimers;
    private PriorityBlockingQueue<QueuedTimer> timerQueue;
    private Thread worker;

    private INetwork network;

    private Properties configuration;


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

    public void registerProtocol(GenericProtocol p) throws ProtocolAlreadyExistsException {
        if(protocolMap.containsKey(p.getProtoId()))
            throw new ProtocolAlreadyExistsException("Protocol conflicts on id with protocol: id=" + p.getProtoId() + ":name=" + protocolMap.get(p.getProtoId()).getProtoName());
        if(protocolByNameMap.containsKey(p.getProtoName()))
            throw new ProtocolAlreadyExistsException("Protocol conflicts on name: " + p.getProtoName() + " (id: " + this.protocolByNameMap.get(p.getProtoName()).getProtoId()+ ")");

        this.protocolMap.put(p.getProtoId(), p);
        this.protocolByNameMap.put(p.getProtoName(), p);
    }

    public GenericProtocol getProtocolByName(String s) {
        GenericProtocol gp = this.protocolByNameMap.get(s);
        return gp;
    }

    public String getProtocolName(short s) {
        GenericProtocol gp = protocolMap.get(s);
        if(gp != null)
            return gp.getProtoName();
        else
            return null;
    }

    public GenericProtocol getProtocol(short s) {
        GenericProtocol gp = protocolMap.get(s);
        return gp;
    }

    public void start(){
        worker.start();
        for(GenericProtocol gp: protocolMap.values())
            gp.start();
    }

    public UUID createPeriodicTimer(ITimerConsumer c, ProtocolTimer t, long first, long period) {
        if(allTimers.containsKey(t.getUuid()))
            return null;
        timerQueue.add(new QueuedTimer(System.currentTimeMillis() + first, period, true, t, c));
        worker.interrupt();
        return t.getUuid();
    }

    public UUID createTimer(ITimerConsumer c, ProtocolTimer t, long timeout) {
        if(allTimers.containsKey(t.getUuid()))
            return null;
        timerQueue.add(new QueuedTimer(System.currentTimeMillis() + timeout, -1, false, t, c));
        worker.interrupt();
        return t.getUuid();
    }

    public ProtocolTimer cancelTimer(UUID timerID) {
        if(!allTimers.containsKey(timerID)) {
            return null;
        }
        timerQueue.remove(allTimers.get(timerID));
        ProtocolTimer t = allTimers.remove(timerID).getEvent();
        worker.interrupt();
        return t;
    }

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
