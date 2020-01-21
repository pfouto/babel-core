package babel.internal;

import babel.GenericProtocol;
import channel.ChannelEvent;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class CustomChannelEventEvent extends InternalEvent {

    ChannelEvent event;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public CustomChannelEventEvent(ChannelEvent evt, int channelId) {
        super(EventType.CUSTOM_CHANNEL_EVENT);
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

    public ChannelEvent getEvent() {
        return event;
    }
}
