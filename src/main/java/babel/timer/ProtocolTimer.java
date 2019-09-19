package babel.timer;

import babel.protocol.event.ProtocolEvent;

import java.util.UUID;

/**
 * An abstract class that represents a timer event
 *
 *
 *
 * @see babel.protocol.event.ProtocolEvent
 *
 * For more information on how timers are handled:
 * @see babel.Babel
 */
public abstract class ProtocolTimer extends ProtocolEvent implements Cloneable {

    private UUID uuid;

    /**
     * Creates an instance of a timer event with the provided numeric id
     * @param timerID numeric id
     */
    public ProtocolTimer(short timerID) {
        super(EventType.TIMER_EVENT, timerID);
        uuid = UUID.randomUUID();
    }

    private ProtocolTimer(short timerID, UUID uuid) {
        super(EventType.TIMER_EVENT, timerID);
        this.uuid = uuid;
    }

    /**
     * Returns the unique id of the timer
     * @return unique id
     */
    public UUID getUuid() {
        return uuid;
    }

    public abstract Object clone();
}
