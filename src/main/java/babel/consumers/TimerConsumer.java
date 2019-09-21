package babel.consumers;


import babel.GenericProtocol;
import babel.internal.TimerEvent;
import babel.protocol.ProtoTimer;

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
public interface TimerConsumer {

    /**
     * Deliver the timer event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param timer the timer to be delivered
     */
    void deliverTimer(TimerEvent t);
}
