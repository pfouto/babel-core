package babel.notification;

import babel.Babel;
import babel.protocol.event.ProtocolEvent;

public abstract class ProtocolNotification extends ProtocolEvent {

    private final String notification;

    private short emitterID;

    public ProtocolNotification(short id, String notification) {
        super(EventType.NOTIFICATION_EVENT, id);
        this.notification = notification;
    }

    public ProtocolNotification(short id, String notification, short emitterID ) {
        super(EventType.NOTIFICATION_EVENT, id);
        this.notification = notification;
        this.emitterID = emitterID;
    }

    public void setEmitter( short id ) {
        this.emitterID = id;
    }

    public String getNotification() {
        return notification;
    }

    public short getEmitterID() {
        return emitterID;
    }

    public String getEmitter() {
        return Babel.getInstance().getProtocolName(this.emitterID);
    }
}
