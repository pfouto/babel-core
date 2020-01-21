package babel.handlers;

import babel.internal.AddressedMessage;
import babel.internal.MessageInEvent;
import channel.ChannelEvent;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #receive(MessageInEvent)}.
 *
 */
@FunctionalInterface
public interface ProtoChannelEventHandler {

    void handleEvent(ChannelEvent event, int channelId);



}
