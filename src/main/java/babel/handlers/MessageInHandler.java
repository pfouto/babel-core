package babel.handlers;

import babel.generic.ProtoMessage;
import network.data.Host;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 */
@FunctionalInterface
public interface MessageInHandler<T extends ProtoMessage> {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param msg the received message
     */
    void receive(T msg, Host from, short sourceProto, int channelId);

}
