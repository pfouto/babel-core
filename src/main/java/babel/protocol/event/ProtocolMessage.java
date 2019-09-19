package babel.protocol.event;

import network.Host;
import network.ISerializer;

/**
 * An abstract class that represents a protocol message
 *
 *
 * @see babel.protocol.event.ProtocolEvent
 * @see babel.protocol.GenericProtocol
 */
public abstract class ProtocolMessage extends ProtocolEvent {

    private volatile Host from;

    /**
     * Create a protocol message event with the provided numeric identifier
     * @param msgID numeric identifier
     */
    public ProtocolMessage(short msgID) {
        super(EventType.MESSAGE_EVENT, msgID);
    }

    /**
     * Returns the IP and port of the process from which the message was sent
     * @return an object Host that contains the IP and port
     */
    public final Host getFrom() { return this.from; }

    /**
     * Set the IP and port of the process from which the message was sent
     * @param from an object Host that contains the IP and port
     */
    public final void setFrom(Host from) { this.from = from; }

}
