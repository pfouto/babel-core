package babel.events;

import babel.generic.GenericProtocol;
import babel.generic.ProtoMessage;
import network.data.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class MessageFailedEvent extends InternalEvent {

    private final ProtoMessage msg;
    private final Host to;
    private final int channelId;
    private final Throwable cause;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public MessageFailedEvent(ProtoMessage msg, Host to, Throwable cause, int channelId) {
        super(EventType.MESSAGE_FAILED_EVENT);
        this.to = to;
        this.msg = msg;
        this.cause = cause;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "MessageFailedEvent{" +
                "msg=" + msg +
                ", to=" + to +
                ", cause=" + cause +
                ", channelId=" + channelId +
                '}';
    }

    public final Host getTo() {
        return to;
    }

    public int getChannelId() {
        return channelId;
    }

    public Throwable getCause() {
        return cause;
    }

    public ProtoMessage getMsg() {
        return msg;
    }

}
