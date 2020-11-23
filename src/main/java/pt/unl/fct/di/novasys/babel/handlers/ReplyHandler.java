package pt.unl.fct.di.novasys.babel.handlers;

import pt.unl.fct.di.novasys.babel.generic.ProtoReply;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 *
 */
@FunctionalInterface
public interface ReplyHandler<T extends ProtoReply> {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param reply the received reply
     */
    void uponReply(T reply, short from);

}
