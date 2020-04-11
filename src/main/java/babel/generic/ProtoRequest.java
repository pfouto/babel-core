package babel.generic;

public abstract class ProtoRequest extends ProtoIPC{

    private final short id;

    public ProtoRequest(short id){
        super(Type.REQUEST);
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
