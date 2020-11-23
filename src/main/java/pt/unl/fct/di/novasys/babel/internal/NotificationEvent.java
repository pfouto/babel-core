package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

public class NotificationEvent extends InternalEvent {

    private final ProtoNotification notification;
    private final short emitterID;

    public NotificationEvent(ProtoNotification notification, short emitterID) {
        super(EventType.NOTIFICATION_EVENT);
        this.notification = notification;
        this.emitterID = emitterID;
    }

    public ProtoNotification getNotification() {
        return notification;
    }

    public short getEmitterID() {
        return emitterID;
    }
}
