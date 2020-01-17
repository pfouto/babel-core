package babel.internal;

import babel.GenericProtocol;
import babel.protocol.ProtoMessage;
import channel.ChannelEvent;
import network.data.Host;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class ChannelEventEvent extends InternalEvent {

    ChannelEvent<AddressedMessage> event;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public ChannelEventEvent(ChannelEvent<AddressedMessage> evt, int channelId) {
        super(EventType.CHANNEL_EVENT);
        this.event = evt;
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "ChannelEvent{" +
                "event=" + event +
                ", channelId=" + channelId +
                '}';
    }

    public int getChannelId() {
        return channelId;
    }

    public ChannelEvent<AddressedMessage> getEvent() {
        return event;
    }
}
