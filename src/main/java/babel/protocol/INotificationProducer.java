package babel.protocol;

import babel.notification.INotificationConsumer;
import babel.exceptions.NotificationDoesNotExistException;

import java.util.Map;

/**
 * An interface of a notification event producer
 *
 *
 * @see babel.protocol.GenericProtocol
 */
public interface INotificationProducer {

    /**
     * Returns the produced notifications
     * in a map : NotificationName -> NotificationID
     * @return a map with the produced notifications
     */
    Map<String, Short> producedNotifications();


    /**
     * Subscribe to the notification with the provided numeric identifier
     * to be consumed by the provided protocol
     * @param notificationID notification numeric identifier
     * @param consumerProtocol protocol to consume the notification
     * @throws NotificationDoesNotExistException if the notifications is not produced
     */
    void subscribeNotification(short notificationID, INotificationConsumer consumerProtocol) throws NotificationDoesNotExistException;

    /**
     * Unsubscribe to the notification with the provided numeric identifier
     * that was consumed by the provided protocol
     * @param notificationID notification numeric identifier
     * @param consumerProtocol protocol that consumed the notification
     */
    void unsubscribeNotification(short notificationID, INotificationConsumer consumerProtocol);

    /**
     * Subscribe to the notification with the provided name
     * to be consumed by the provided protocol
     * @param notification notification name
     * @param consumerProtocol protocol to consume the notification
     * @throws NotificationDoesNotExistException if the notifications is not produced
     */
    void subscribeNotification(String notification, INotificationConsumer consumerProtocol) throws NotificationDoesNotExistException;

    /**
     * Unsubscribe to the notification with the provided name
     * that was consumed by the provided protocol
     * @param notification notification name
     * @param consumerProtocol protocol that consumed the notification
     */
    void unsubscribeNotification(String notification, INotificationConsumer consumerProtocol);
}
