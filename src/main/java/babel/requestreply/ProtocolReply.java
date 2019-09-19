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
public abstract class ProtocolReply extends ProtocolInteraction {

    public ProtocolReply() {
        super(EventType.REPLY_EVENT);
    }

    public ProtocolReply(short id) {
        super(EventType.REPLY_EVENT, id);
    }

    public ProtocolReply(short id, short sender, short destination) {
        super(EventType.REPLY_EVENT, id, sender, destination);
    }

}
