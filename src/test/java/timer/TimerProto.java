package timer;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.metrics.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

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
