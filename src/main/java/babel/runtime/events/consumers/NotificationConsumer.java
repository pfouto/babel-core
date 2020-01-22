package babel.runtime.events.consumers;

import babel.genericprotocol.GenericProtocol;
import babel.runtime.events.NotificationEvent;

/**
 * An interface of a request event consumer
 *
 *
 * @see GenericProtocol
 */
public interface NotificationConsumer {

    /**
     * Deliver the notification event to the correct protocols
     *
     * Can be implemented differently by applications
     *
     * @param notification the notification to be delivered
     */
    void deliverNotification(NotificationEvent notification);
}
