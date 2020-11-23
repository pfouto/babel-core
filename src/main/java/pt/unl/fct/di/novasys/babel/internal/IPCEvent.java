package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.generic.ProtoIPC;

public class IPCEvent extends InternalEvent {

    private final ProtoIPC ipc;
    private final short senderID;
    private final short destinationID;

    public IPCEvent(ProtoIPC ipc, short sender, short destination) {
        super(EventType.IPC_EVENT);
        this.ipc = ipc;
        this.senderID = sender;
        this.destinationID = destination;
    }

    public ProtoIPC getIpc() {
        return ipc;
    }

    public short getDestinationID() {
        return destinationID;
    }

    public short getSenderID() {
        return senderID;
    }
}
