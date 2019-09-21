package babel.protocol;

public abstract class ProtoTimer implements Cloneable {

    private final short id;

    public ProtoTimer(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public abstract Object clone();
}
