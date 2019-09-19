package babel.notification;

import babel.Babel;
import babel.protocol.event.ProtocolEvent;

/**
 * An abstract class that represents a notification event
 *
 *
 *
 * @see babel.protocol.event.ProtocolEvent
 * @see babel.protocol.GenericProtocol
 */
public abstract class ProtocolNotification extends ProtocolEvent {

    private final String notification;

    private short emitterID;

    /**
     * Creates a notification event with the provided numeric identifier
     * and name
     * @param id numeric identifier
     * @param notification name
     */
    public ProtocolNotification(short id, String notification) {
        super(EventType.NOTIFICATION_EVENT, id);
        this.notification = notification;
    }

    /**
     * Creates a notification event with the provided numeric identifier
     * and name, emitted by the provided protocol numeric identifier
     * @param id numeric identifier of the notification
     * @param notification name of the notification
     * @param emitterID numeric identifier of the protocol that emits the notification
     */
    public ProtocolNotification(short id, String notification, short emitterID ) {
        super(EventType.NOTIFICATION_EVENT, id);
        this.notification = notification;
        this.emitterID = emitterID;
    }

    /**
     * Sets the protocol numeric identifier that emits the notification
     * @param id numeric identifier
     */
    public void setEmitter( short id ) {
        this.emitterID = id;
    }

    /**
     * Returns the name of the notification
     * @return name
     */
    public String getNotification() {
        return notification;
    }

    /**
     * Returns the numeric identifier of the protocol that emits the notification
     * @return numeric identifier
     */
    public short getEmitterID() {
        return emitterID;
    }

    /**
     * Returns the name of the protocol that emits the notification
     * @return name
     */
    public String getEmitter() {
        return Babel.getInstance().getProtocolName(this.emitterID);
    }
}
