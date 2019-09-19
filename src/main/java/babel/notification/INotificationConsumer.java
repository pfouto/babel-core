package babel.notification;

public interface INotificationConsumer {

    void deliverNotification(ProtocolNotification notification);
}
