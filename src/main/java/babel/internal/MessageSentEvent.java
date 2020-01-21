package babel.internal;

import babel.GenericProtocol;
import network.data.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class MessageSentEvent extends InternalEvent {

    private final AddressedMessage msg;
    private final Host to;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public MessageSentEvent(AddressedMessage msg, Host to, int channelId) {
        super(EventType.MESSAGE_SENT_EVENT);
        this.to = to;
        this.msg = msg;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "MessageSentEvent{" +
                "msg=" + msg +
                ", to=" + to +
                ", channelId=" + channelId +
                '}';
    }

    public final Host getTo() {
        return to;
    }

    public int getChannelId() {
        return channelId;
    }

    public AddressedMessage getMsg() {
        return msg;
    }

}
