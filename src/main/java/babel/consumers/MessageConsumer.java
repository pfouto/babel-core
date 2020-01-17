package babel.consumers;


import babel.GenericProtocol;
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
public interface MessageConsumer {

    /**
     * Deliver the timer event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param t the timer to be delivered
     */
    void deliverMessage(MessageEvent t);
}
