package babel.runtime.events;

import babel.runtime.protocol.ProtoTimer;
import babel.runtime.events.consumers.TimerConsumer;

import java.util.Comparator;

public class TimerEvent extends InternalEvent implements Comparable<TimerEvent>, Comparator<TimerEvent> {

    private long uuid;
    private ProtoTimer timer;

    private final TimerConsumer consumer;
    private long triggerTime;
    private final boolean periodic;
    private final long period;

    public TimerEvent(ProtoTimer timer, long uuid, TimerConsumer consumer, long triggerTime, boolean periodic,
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

    public TimerConsumer getConsumer() {
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
