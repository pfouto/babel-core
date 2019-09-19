package babel.requestreply;

import babel.protocol.event.ProtocolInteraction;

/**
 * An abstract class that represents a request event
 *
 *
 *
 * @see babel.protocol.event.ProtocolEvent
 * @see babel.protocol.GenericProtocol
 */
public abstract class ProtocolRequest extends ProtocolInteraction {

    /**
     * Creates a request event with the provided numeric identifier
     * @param id numeric identifier
     */
    public ProtocolRequest(short id) {
        super(EventType.REQUEST_EVENT, id);
    }

    /**
     * Creates a request event with the provided numeric identifier
     * and the sender and destination protocol's numeric identifier
     * @param id numeric identifier of the request event
     * @param sender numeric identifier of the sender protocol
     * @param destination numeric identifier of the destination protocol
     */
    public ProtocolRequest(short id, short sender, short destination) {
        super(EventType.REQUEST_EVENT, id, sender, destination);
    }
}
