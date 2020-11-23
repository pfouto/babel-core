package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.generic.ProtoTimer;

import java.util.Comparator;

public class TimerEvent extends InternalEvent implements Comparable<TimerEvent>, Comparator<TimerEvent> {

    private final long uuid;
    private final ProtoTimer timer;

    private final GenericProtocol consumer;
    private final boolean periodic;
    private final long period;

    private long triggerTime;

    public TimerEvent(ProtoTimer timer, long uuid, GenericProtocol consumer, long triggerTime, boolean periodic,
                      long period) {
        super(EventType.TIMER_EVENT);
        this.timer = timer;
        this.uuid = uuid;
        this.consumer = consumer;
        this.triggerTime = triggerTime;
        this.period = period;
        this.periodic = periodic;
    }

    public ProtoTimer getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        return "TimerEvent{" +
                "uuid=" + uuid +
                ", timer=" + timer +
                ", consumer=" + consumer +
                ", triggerTime=" + triggerTime +
                ", periodic=" + periodic +
                ", period=" + period +
                '}';
    }

    public long getUuid() {
        return uuid;
    }

    public long getPeriod() {
        return period;
    }

    public boolean isPeriodic() {
        return periodic;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public GenericProtocol getConsumer() {
        return consumer;
    }

    public void setTriggerTime(long triggerTime) {
        this.triggerTime = triggerTime;
    }

    @Override
    public int compareTo(TimerEvent o) {
        return Long.compare(this.triggerTime, o.triggerTime);
    }

    @Override
    public int compare(TimerEvent o1, TimerEvent o2) {
        return Long.compare(o1.triggerTime, o2.triggerTime);
    }

}
