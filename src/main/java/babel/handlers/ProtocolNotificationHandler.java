package babel.handlers;

import babel.notification.ProtocolNotification;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponNotification(ProtocolNotification)}.
 *
 */
@FunctionalInterface
public interface ProtocolNotificationHandler {

    /**
     * Performs this operation on the ProtocolNotification.
     *
     * @param n the input argument
     */
    void uponNotification(ProtocolNotification n);

}
