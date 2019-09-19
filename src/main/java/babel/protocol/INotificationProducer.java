package babel.protocol;

import babel.notification.INotificationConsumer;
import babel.exceptions.NotificationDoesNotExistException;

import java.util.Map;

public interface INotificationProducer {

    Map<String, Short> producedNotifications();

    void subscribeNotification(short notificationID, INotificationConsumer c) throws NotificationDoesNotExistException;

    void unsubscribeNotification(short notificationID, INotificationConsumer c);

    void subscribeNotification(String notification, INotificationConsumer c) throws NotificationDoesNotExistException;

    void unsubscribeNotification(String notification, INotificationConsumer c);
}
