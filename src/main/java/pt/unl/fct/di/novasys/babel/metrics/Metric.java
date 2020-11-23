package pt.unl.fct.di.novasys.babel.metrics;

import java.util.function.Consumer;

public abstract class Metric {

    private final String name;
    private final boolean resetOnLog;
    private final boolean logPeriodically;
    private final long period;
    private final boolean logOnChange;

    private Consumer<Metric> onChangeHandler;

    public Metric(String name, boolean logPeriodically, long period, boolean logOnChange, boolean resetOnLog) {
        this.name = name;
        this.logOnChange = logOnChange;
        this.logPeriodically = logPeriodically;
        this.period = period;
        this.resetOnLog = resetOnLog;
        this.onChangeHandler = null;
    }

    public String getName() {
        return name;
    }

    public boolean isResetOnLog() {
        return resetOnLog;
    }

    public boolean isLogPeriodically() {
        return logPeriodically;
    }

    public long getPeriod() {
        return period;
    }

    public boolean isLogOnChange() {
        return logOnChange;
    }

    protected void setOnChangeHandler(Consumer<Metric> handler) {
        this.onChangeHandler = handler;
    }

    protected void onChange() {
        if (onChangeHandler != null)
            onChangeHandler.accept(this);
    }

    protected abstract void reset();

    protected abstract String computeValue();

}
