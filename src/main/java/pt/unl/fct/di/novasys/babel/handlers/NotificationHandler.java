package pt.unl.fct.di.novasys.babel.handlers;

import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 *
 */
@FunctionalInterface
public interface NotificationHandler<T extends ProtoNotification> {

    /**
     * Performs this operation on the ProtocolNotification.
     *
     * @param notification the received notification
     */
    void uponNotification(T notification, short emitter);

}
