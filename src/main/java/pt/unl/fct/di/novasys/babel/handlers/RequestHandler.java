package pt.unl.fct.di.novasys.babel.handlers;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 *
 */
@FunctionalInterface
public interface RequestHandler<T extends ProtoRequest> {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param request the received request
     */
    void uponRequest(T request, short from);

}
