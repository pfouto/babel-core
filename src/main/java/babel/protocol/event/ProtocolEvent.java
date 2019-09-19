package babel.protocol.event;

/**
 * An abstract class that represent a generic event
 *
 * @see ProtocolMessage
 * @see babel.timer.ProtocolTimer
 * @see babel.notification.ProtocolNotification
 * @see babel.requestreply.ProtocolReply
 * @see babel.requestreply.ProtocolRequest
 */
public abstract class ProtocolEvent {

    /**
     * Possible event types that can be represented
     */
    public enum EventType {
        MESSAGE_EVENT, TIMER_EVENT, NOTIFICATION_EVENT, REQUEST_EVENT, REPLY_EVENT
    }

    private transient final EventType t;
    private transient short id;

    /**
     * Create a protocol event of the provided event type
     * @param type the event type
     */
    public ProtocolEvent(EventType type) {
        this.t = type;
    }

    /**
     * Create a protocol event of the provided event type
     * and with the provided numeric identifier
     * @param type the event type
     * @param id the numeric identifier
     */
    public ProtocolEvent(EventType type, short id) {
        this.t = type;
        this.id = id;
    }

    /**
     * Returns the event type of the protocol event
     * @return event type
     */
    public final EventType getType() {
        return t;
    }

    /**
     * Verifies if the event a message
     * @return true if is of type MESSAGE_EVENT; false otherwise
     */
    public final boolean isMessageEvent() {
        return this.t == EventType.MESSAGE_EVENT;
    }

    /**
     * Verifies if the event is a timer
     * @return true if is of type TIMER_EVENT; false otherwise
     */
    public final boolean isTimerEvent() {
        return this.t == EventType.TIMER_EVENT;
    }

    /**
     * Verifies if the event is notification
     * @return true if is of type NOTIFICATION_EVENT; false otherwise
     */
    public final boolean isNotificationEvent() { return this.t == EventType.NOTIFICATION_EVENT; }

    /**
     * Verifies if the event is a request
     * @return true if is of type REQUEST_EVENT; false otherwise
     */
    public final boolean isRequestEvent() { return this.t == EventType.REQUEST_EVENT; }

    /**
     * Verifies if the event is a reply
     * @return true if is of type REPLY_EVENT; false otherwise
     */
    public final boolean isReplyEvent() { return this.t == EventType.REPLY_EVENT; }

    /**
     * Returns the numeric identifier of the event
     * @return numeric identifier
     */
    public short getId() {
        return id;
    }

    /**
     * Sets the numeric identifier of the event
     * @param id numeric identifier
     */
    public void setId(short id) {
        this.id = id;
    }
}
