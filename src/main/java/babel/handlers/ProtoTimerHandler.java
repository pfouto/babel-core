package babel.handlers;

import babel.internal.TimerEvent;
import babel.protocol.ProtoTimer;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponTimer}.
 *
 */
@FunctionalInterface
public interface ProtoTimerHandler {

    /**
     * Performs this operation on the ProtocolTimer.
     *
     * @param timer the received timer
     */
    void uponTimer(ProtoTimer timer, long uId);

}
