package babel.generic;

public abstract class ProtoTimer implements Cloneable {

    private final short id;

    public ProtoTimer(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public abstract ProtoTimer clone();
}