package babel.timer;

import babel.protocol.event.ProtocolEvent;

import java.util.UUID;

public abstract class ProtocolTimer extends ProtocolEvent implements Cloneable {

    private UUID uuid;

    public ProtocolTimer(short timerId) {
        super(EventType.TIMER_EVENT, timerId);
        uuid = UUID.randomUUID();
    }

    private ProtocolTimer(short timerId, UUID uuid) {
        super(EventType.TIMER_EVENT, timerId);
        uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public abstract Object clone();
}
