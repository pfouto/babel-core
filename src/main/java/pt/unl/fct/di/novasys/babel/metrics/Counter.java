package pt.unl.fct.di.novasys.babel.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class Counter extends Metric {

    private AtomicLong value;

    public Counter(String name, boolean logPeriodically, long period, boolean logOnChange, boolean resetOnLog) {
        super(name, logPeriodically, period, logOnChange, resetOnLog);
        value = new AtomicLong();
    }

    public synchronized void inc() {
        value.incrementAndGet();
        onChange();
    }

    public long getValue() {
        return value.get();
    }

    @Override
    protected synchronized void reset() {
        value.set(0);
    }

    @Override
    protected synchronized String computeValue() {
        return String.valueOf(getValue());
    }
}
