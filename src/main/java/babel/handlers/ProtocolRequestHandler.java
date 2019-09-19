package babel.handlers;

import babel.requestreply.ProtocolRequest;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponRequest(ProtocolRequest)}.
 *
 */
@FunctionalInterface
public interface ProtocolRequestHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param request the received request
     */
    void uponRequest(ProtocolRequest request);

}
