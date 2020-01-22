package babel.runtime.events.consumers;

import babel.runtime.events.CustomChannelEvent;
import babel.runtime.events.MessageFailedEvent;
import babel.runtime.events.MessageInEvent;
import babel.runtime.events.MessageSentEvent;

public interface ChannelConsumer {

    void deliverChannelEvent(CustomChannelEvent event);

    void deliverMessageSent(MessageSentEvent event);

    void deliverMessageFailed(MessageFailedEvent event);

    void deliverMessageIn(MessageInEvent t);

}
