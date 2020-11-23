package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.network.data.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class MessageInEvent extends InternalEvent {

    private final BabelMessage msg;
    private final Host from;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public MessageInEvent(BabelMessage msg, Host from, int channelId) {
        super(EventType.MESSAGE_IN_EVENT);
        this.from = from;
        this.msg = msg;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "MessageInEvent{" +
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

    public BabelMessage getMsg() {
        return msg;
    }

}
