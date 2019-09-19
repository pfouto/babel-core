package babel.handlers;

import babel.requestreply.ProtocolReply;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponReply(ProtocolReply)}.
 *
 */
@FunctionalInterface
public interface ProtocolReplyHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param r the input argument
     */
    void uponReply(ProtocolReply r);

}
