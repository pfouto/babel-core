package babel.runtime.events.consumers;


import babel.genericprotocol.GenericProtocol;
import babel.runtime.Babel;
import babel.runtime.events.TimerEvent;

/**
 * An interface of a timer event consumer
 *
 *
 * Implemented by:
 * @see GenericProtocol
 *
 * Used in:
 * @see Babel
 */
public interface TimerConsumer {

    /**
     * Deliver the timer event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param t the timer to be delivered
     */
    void deliverTimer(TimerEvent t);
}
