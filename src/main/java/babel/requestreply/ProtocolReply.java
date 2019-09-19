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
public abstract class ProtocolReply extends ProtocolInteraction {

    /**
     * Creates a reply event
     */
    public ProtocolReply() {
        super(EventType.REPLY_EVENT);
    }

    /**
     * Creates a reply event with the numeric identifier provided
     * @param id numeric identifier
     */
    public ProtocolReply(short id) {
        super(EventType.REPLY_EVENT, id);
    }

    /**
     * Creates a reply event with the numeric identifier provided
     * and the sender and destination protocol's numeric identifier
     * @param id numeric identifier of the reply event
     * @param sender numeric identifier of the sender protocol
     * @param destination numeric identifier of the destination protocol
     */
    public ProtocolReply(short id, short sender, short destination) {
        super(EventType.REPLY_EVENT, id, sender, destination);
    }

}
