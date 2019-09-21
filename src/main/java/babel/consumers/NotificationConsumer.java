package babel.consumers;

import babel.GenericProtocol;
import babel.internal.NotificationEvent;

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
