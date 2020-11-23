package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.channel.ChannelEvent;

/**
 * An abstract class that represents a protocol message
 *
 * @see InternalEvent
 * @see GenericProtocol
 */
public class CustomChannelEvent extends InternalEvent {

    private final ChannelEvent event;
    private final int channelId;

    /**
     * Create a protocol message event with the provided numeric identifier
     */
    public CustomChannelEvent(ChannelEvent evt, int channelId) {
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
