package babel.requestreply;

import babel.protocol.event.ProtocolInteraction;

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
