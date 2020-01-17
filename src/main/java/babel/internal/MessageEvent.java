package babel.internal;

import babel.GenericProtocol;
import babel.protocol.ProtoMessage;
import network.data.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class MessageEvent extends InternalEvent {

    private final AddressedMessage msg;
    private final Host from;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public MessageEvent(AddressedMessage msg, Host from, int channelId) {
        super(EventType.MESSAGE_EVENT);
        this.from = from;
        this.msg = msg;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "BabelMessage{" +
                "msg=" + msg +
                ", from=" + from +
                ", channelId=" + channelId +
                '}';
    }

    public final Host getFrom() {
        return this.from;
    }

    public int getChannelId() {
        return channelId;
    }

    public AddressedMessage getMsg() {
        return msg;
    }

}
