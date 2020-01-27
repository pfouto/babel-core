package babel.events.consumers;

import babel.events.IPCEvent;

public interface IPCConsumer {

    void deliverIPC(IPCEvent ipc);
}
