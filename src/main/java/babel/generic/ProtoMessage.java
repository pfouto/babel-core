package babel.generic;

public abstract class ProtoMessage {

    private final short id;
    short sourceProto;
    short destProto;

    public ProtoMessage(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
