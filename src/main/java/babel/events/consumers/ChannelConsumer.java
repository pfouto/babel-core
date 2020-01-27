package babel.events.consumers;

import babel.events.CustomChannelEvent;
import babel.events.MessageFailedEvent;
import babel.events.MessageInEvent;
import babel.events.MessageSentEvent;

public interface ChannelConsumer {

    void deliverChannelEvent(CustomChannelEvent event);

    void deliverMessageSent(MessageSentEvent event);

    void deliverMessageFailed(MessageFailedEvent event);

    void deliverMessageIn(MessageInEvent t);

}
