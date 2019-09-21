package babel.internal;

import babel.GenericProtocol;
import babel.protocol.ProtoMessage;
import network.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class MessageInEvent extends InternalEvent {

    private final ProtoMessage msg;
    private final Host from;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public MessageInEvent(ProtoMessage msg, Host from) {
        super(EventType.MESSAGE_EVENT);
        this.from = from;
        this.msg = msg;
    }

    /**
     * Returns the IP and port of the process from which the message was sent
     * @return an object Host that contains the IP and port
     */
    public final Host getFrom() { return this.from; }

    public ProtoMessage getMsg() {
        return msg;
    }
}
