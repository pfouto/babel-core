package babel.consumers;

import babel.internal.IPCEvent;

public interface IPCConsumer {

    void deliverIPC(IPCEvent ipc);
}
