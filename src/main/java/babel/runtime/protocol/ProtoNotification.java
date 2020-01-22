package babel.runtime.protocol;

public abstract class ProtoNotification {

    private final short id;

    public ProtoNotification(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

}
