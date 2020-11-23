package pt.unl.fct.di.novasys.babel.metrics;

import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Reducer extends Metric {

    public enum Operation {AVG, MAX, MIN}

    private final ConcurrentLinkedQueue<Double> collection;
    private final Operation op;

    public Reducer(String name, boolean logPeriodically, long period, boolean resetOnLog, Operation op) {
        super(name, logPeriodically, period, false, resetOnLog);
        this.op = op;
        collection = new ConcurrentLinkedQueue<>();
    }

    public synchronized void add(Double val) {
        collection.add(val);
    }

    @Override
    protected synchronized void reset() {
        collection.clear();
    }

    @Override
    protected synchronized String computeValue() {
        String res;
        int nEntries = collection.size();
        switch (op) {
            case AVG:
                OptionalDouble val = collection.stream().mapToDouble(i -> i).average();
                res = val.isPresent() ? String.valueOf(val.getAsDouble()) : "null";
                break;
            case MAX:
                OptionalDouble maxVal = collection.stream().mapToDouble(i -> i).max();
                res = maxVal.isPresent() ? String.valueOf(maxVal.getAsDouble()) : "null";
                break;
            case MIN:
                OptionalDouble minVal = collection.stream().mapToDouble(i -> i).min();
                res = minVal.isPresent() ? String.valueOf(minVal.getAsDouble()) : "null";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + op);
        }
        return res + " " + nEntries;
    }
}
