package babel.protocol.event;

import network.Host;
import network.ISerializer;

public abstract class ProtocolMessage extends ProtocolEvent {

    private volatile Host from;

    public ProtocolMessage(short msgId) {
        super(EventType.MESSAGE_EVENT, msgId);
    }

    public final Host getFrom() { return this.from; }

    public final void setFrom(Host from) { this.from = from; }

}
