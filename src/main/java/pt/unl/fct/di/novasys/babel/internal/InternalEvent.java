package pt.unl.fct.di.novasys.babel.internal;

/**
 * An abstract class that represent a generic event
 *
 * @see MessageInEvent
 * @see TimerEvent
 * @see NotificationEvent
 * @see IPCEvent
 */
public abstract class InternalEvent {

    /**
     * Possible event types that can be represented
     */
    public enum EventType {
        MESSAGE_IN_EVENT,
        TIMER_EVENT,
        NOTIFICATION_EVENT,
        IPC_EVENT,
        MESSAGE_SENT_EVENT,
        MESSAGE_FAILED_EVENT,
        CUSTOM_CHANNEL_EVENT
    }

    private transient final EventType t;

    /**
     * Create a protocol event of the provided event type
     * @param type the event type
     */
    public InternalEvent(EventType type) {
        this.t = type;
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
        return this.t == EventType.MESSAGE_IN_EVENT;
    }

    /**
     * Verifies if the event is a channel event
     * @return true if is of type CHANNEL_EVENT; false otherwise
     */
    public final boolean isCustomChannelEvent() {
        return this.t == EventType.CUSTOM_CHANNEL_EVENT;
    }
    public final boolean isMessageSentEvent() {
        return this.t == EventType.MESSAGE_SENT_EVENT;
    }
    public final boolean isMessageFailedEvent() {
        return this.t == EventType.MESSAGE_FAILED_EVENT;
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
     * Verifies if the event is an IPC
     * @return true if is of type IPC_EVENT; false otherwise
     */
    public final boolean isIPCEvent() { return this.t == EventType.IPC_EVENT; }

}
