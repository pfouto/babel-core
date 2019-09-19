package babel.handlers;

import babel.protocol.event.ProtocolMessage;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #receive(ProtocolMessage)}.
 *
 */
@FunctionalInterface
public interface ProtocolMessageHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param m the input argument
     */
    void receive(ProtocolMessage m);

}
