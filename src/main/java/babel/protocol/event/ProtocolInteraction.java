package babel.protocol.event;

import babel.Babel;

public abstract class ProtocolInteraction extends ProtocolEvent{

    private short senderID;

    private short destinationID;

    public ProtocolInteraction(EventType t) {
        super(t);
    }

    public ProtocolInteraction(EventType t, short id) {
        super(t, id);
    }

    public ProtocolInteraction(EventType t, short id, short sender, short destination) {
        super(t, id);
        this.senderID = sender;
        this.destinationID = destination;
    }

    public short getDestinationID() {
        return destinationID;
    }

    public String getDestination() {
        return Babel.getInstance().getProtocolName(destinationID);
    }

    public void setDestination(short destinationID) {
        this.destinationID = destinationID;
    }

    public short getSenderID() {
        return senderID;
    }

    public String getSender() {
        return Babel.getInstance().getProtocolName(senderID);
    }

    public void invertDestination(ProtocolInteraction r) {
        this.destinationID = r.senderID;
        this.senderID = r.destinationID;
    }

    public void setSender(short senderID) {
        this.senderID = senderID;
    }
}
