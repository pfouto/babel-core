package timer;

import pt.unl.fct.di.novasys.babel.core.Babel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerTest {

    static {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    private static final Logger logger = LogManager.getLogger(TimerTest.class);

    public static void main(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        logger.info("Hello, I am alive");

        TimerProto proto = new TimerProto();

        logger.info("Init: " + System.currentTimeMillis());
        proto.init(null);

        babel.registerProtocol(proto);

        Thread.sleep(500);

        logger.info("Start: " + System.currentTimeMillis());
        babel.start();
    }
}