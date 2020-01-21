package babel.consumers;

import babel.internal.CustomChannelEventEvent;
import babel.internal.MessageFailedEvent;
import babel.internal.MessageInEvent;
import babel.internal.MessageSentEvent;

public interface ChannelConsumer {

    void deliverChannelEvent(CustomChannelEventEvent event);

    void deliverMessageSent(MessageSentEvent event);

    void deliverMessageFailed(MessageFailedEvent event);

    void deliverMessageIn(MessageInEvent t);

}
