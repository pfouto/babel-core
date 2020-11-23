package timer;

import babel.core.GenericProtocol;
import babel.exceptions.HandlerRegistrationException;
import babel.metrics.Counter;
import babel.metrics.Instant;
import babel.metrics.MetricsManager;
import babel.metrics.Reducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class TimerProto extends GenericProtocol {

    private static final Logger logger = LogManager.getLogger(TimerProto.class);

    private Instant instantLogger;

    public TimerProto() {
        super("TimerTest", (short) 100);
    }

    @Override
    public void init(Properties props) throws HandlerRegistrationException, IOException {
        instantLogger = new Instant("Time");
        registerMetric(instantLogger);

        registerTimerHandler(TimerTimer.TIMER_ID, this::handleTimerTimer);
        setupPeriodicTimer(new TimerTimer(), 1000, 300);
    }

    private void handleTimerTimer(TimerTimer timer, long timerId) {
        instantLogger.log(System.currentTimeMillis());
    }

}
