package babel.timer;


/**
 * An interface of a timer event consumer
 *
 *
 * Implemented by:
 * @see babel.protocol.GenericProtocol
 *
 * Used in:
 * @see babel.Babel
 */
public interface ITimerConsumer {

    /**
     * Deliver the timer event to the correct protocol
     *
     * Can be implemented differently by applications
     *
     * @param timer the timer to be delivered
     */
    void deliverTimer(ProtocolTimer timer);
}
