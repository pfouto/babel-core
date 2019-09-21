package babel.handlers;

import babel.internal.NotificationEvent;
import babel.protocol.ProtoNotification;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponNotification(NotificationEvent)}.
 *
 */
@FunctionalInterface
public interface ProtoNotificationHandler {

    /**
     * Performs this operation on the ProtocolNotification.
     *
     * @param notification the received notification
     */
    void uponNotification(ProtoNotification notification, short emitter);

}
