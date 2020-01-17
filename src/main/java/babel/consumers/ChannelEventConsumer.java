package babel.consumers;


import babel.GenericProtocol;
import babel.internal.ChannelEventEvent;
import babel.internal.MessageEvent;

/**
 * An interface of a timer event consumer
 *
 *
 * Implemented by:
 * @see GenericProtocol
 *
 * Used in:
 * @see babel.Babel
 */
public interface ChannelEventConsumer {

    void deliverChannelEvent(ChannelEventEvent event);
}
