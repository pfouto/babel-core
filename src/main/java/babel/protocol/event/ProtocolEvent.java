package babel.protocol.event;

public abstract class ProtocolEvent {

    public enum EventType {
        MESSAGE_EVENT, TIMER_EVENT, NOTIFICATION_EVENT, REQUEST_EVENT, REPLY_EVENT
    }

    private transient final EventType t;
    private transient short id;

    public ProtocolEvent(EventType t) {
        this.t = t;
    }

    public ProtocolEvent(EventType t, short id) {
        this.t = t;
        this.id = id;
    }

    public final EventType getType() {
        return t;
    }

    public final boolean isInMessage() {
        return this.t == EventType.MESSAGE_EVENT;
    }

    public final boolean isTimerEvent() {
        return this.t == EventType.TIMER_EVENT;
    }

    public final boolean isNotification() { return this.t == EventType.NOTIFICATION_EVENT; }

    public final boolean isRequest() { return this.t == EventType.REQUEST_EVENT; }

    public final boolean isReply() { return this.t == EventType.REPLY_EVENT; }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }
}
