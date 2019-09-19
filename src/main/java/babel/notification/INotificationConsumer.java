package babel.notification;

/**
 * An interface of a request event consumer
 *
 *
 * @see babel.protocol.GenericProtocol
 */
public interface INotificationConsumer {

    /**
     * Deliver the notification event to the correct protocols
     *
     * Can be implemented differently by applications
     *
     * @param notification the notification to be delivered
     */
    void deliverNotification(ProtocolNotification notification);
}
