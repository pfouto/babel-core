package pt.unl.fct.di.novasys.babel.metrics;

import pt.unl.fct.di.novasys.babel.core.Babel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsManager {

    private static final Logger logger = LogManager.getLogger(MetricsManager.class);

    private static MetricsManager system;

    public static synchronized MetricsManager getInstance() {
        if (system == null)
            system = new MetricsManager();
        return system;
    }

    private final Queue<Metric> toSchedule;

    private final ScheduledExecutorService scheduler;
    private boolean started;

    MetricsManager() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        toSchedule = new LinkedList<>();
        started = false;
    }

    public synchronized void start() {
        started = true;
        Iterator<Metric> iterator = toSchedule.iterator();
        while (iterator.hasNext()) {
            scheduleMetric(iterator.next());
            iterator.remove();
        }
    }

    private void scheduleMetric(Metric m) {
        scheduler.scheduleAtFixedRate(() -> logMetric(m), m.getPeriod(), m.getPeriod(), TimeUnit.MILLISECONDS);
    }

    public synchronized void registerMetric(Metric m) {
        if (m.isLogPeriodically()) {
            if (started) scheduleMetric(m);
            else toSchedule.add(m);
        }
        if (m.isLogOnChange()) {
            m.setOnChangeHandler(this::logMetric);
        }
    }

    public void logMetric(Metric m) {
        synchronized (m) {
            logger.info("[" + Babel.getInstance().getMillisSinceStart() + "] " + m.getName() + " " + m.computeValue());
            if (m.isResetOnLog()) m.reset();
        }
    }
}
