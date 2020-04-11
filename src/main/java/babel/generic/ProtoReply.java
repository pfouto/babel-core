package babel.generic;

public abstract class ProtoReply extends ProtoIPC{

    private final short id;

    public ProtoReply(short id){
        super(Type.REPLY);
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
