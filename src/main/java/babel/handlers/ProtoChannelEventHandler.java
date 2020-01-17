package babel.handlers;

import babel.internal.AddressedMessage;
import babel.internal.ChannelEventEvent;
import babel.internal.MessageEvent;
import babel.protocol.ProtoMessage;
import channel.ChannelEvent;
import network.data.Host;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #receive(MessageEvent)}.
 *
 */
@FunctionalInterface
public interface ProtoChannelEventHandler {

    /**
     * Performs this operation on the ProtocolMessage.
     *
     * @param msg the received message
     */
    void handleEvent(ChannelEvent<AddressedMessage> event, int channelId);

}
