package babel.handlers;

import babel.internal.MessageInEvent;
import babel.protocol.ProtoMessage;
import network.data.Host;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 */
@FunctionalInterface
public interface ProtoMessageHandler<T extends ProtoMessage> {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param msg the received message
     */
    void receive(T msg, Host from, int sourceProto, int channelId);

}
