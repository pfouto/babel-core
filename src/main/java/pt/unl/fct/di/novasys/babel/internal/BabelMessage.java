package pt.unl.fct.di.novasys.babel.internal;

import pt.unl.fct.di.novasys.babel.generic.ProtoMessage;

public class BabelMessage {

    private final ProtoMessage message;
    private final short sourceProto;
    private final short destProto;

    @Override
    public String toString() {
        return "BabelMessage{" +
                "message=" + message +
                ", sourceProto=" + sourceProto +
                ", destProto=" + destProto +
                '}';
    }

    public BabelMessage(ProtoMessage message, short sourceProto, short destProto) {
        this.message = message;
        this.sourceProto = sourceProto;
        this.destProto = destProto;
    }

    public ProtoMessage getMessage() {
        return message;
    }

    public short getSourceProto() {
        return sourceProto;
    }

    public short getDestProto() {
        return destProto;
    }
}
