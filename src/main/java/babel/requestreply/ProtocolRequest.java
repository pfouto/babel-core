package babel.requestreply;

import babel.protocol.event.ProtocolInteraction;

/**
 * An abstract class that represents a request event
 *
 *
 *
 *
 * @see babel.protocol.GenericProtocol
 */
public abstract class ProtocolRequest extends ProtocolInteraction {

    public ProtocolRequest(short id) {
        super(EventType.REQUEST_EVENT, id);
    }

    public ProtocolRequest(short id, short sender, short destination) {
        super(EventType.REQUEST_EVENT, id, sender, destination);
    }
}
