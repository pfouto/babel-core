package babel.handlers;

import babel.protocol.ProtoReply;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #uponReply(ReplyEvent)}.
 *
 */
@FunctionalInterface
public interface ProtoReplyHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param reply the received reply
     */
    void uponReply(ProtoReply reply, short from);

}
