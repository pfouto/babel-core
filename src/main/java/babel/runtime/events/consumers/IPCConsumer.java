package babel.runtime.events.consumers;

import babel.runtime.events.IPCEvent;

public interface IPCConsumer {

    void deliverIPC(IPCEvent ipc);
}
