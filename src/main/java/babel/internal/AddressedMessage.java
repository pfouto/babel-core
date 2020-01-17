package babel.internal;

import babel.protocol.ProtoMessage;

public class AddressedMessage {

    private final ProtoMessage msg;
    private final short sourceProto;
    private final short destProto;

    public AddressedMessage(ProtoMessage msg, short source, short dest) {
        this.msg = msg;
        this.sourceProto = source;
        this.destProto = dest;
    }

    @Override
    public String toString() {
        return "AddressedMessage{" +
                "msg=" + msg +
                ", sourceProto=" + sourceProto +
                ", destProto=" + destProto +
                '}';
    }

    public ProtoMessage getMsg() {
        return msg;
    }

    public short getDestProto() {
        return destProto;
    }

    public short getSourceProto() {
        return sourceProto;
    }

}
