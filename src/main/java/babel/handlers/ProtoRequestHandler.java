package babel.handlers;
import babel.protocol.ProtoRequest;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponRequest(RequestEvent)}.
 *
 */
@FunctionalInterface
public interface ProtoRequestHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param request the received request
     */
    void uponRequest(ProtoRequest request, short from);

}
