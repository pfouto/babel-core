package babel.generic;

public abstract class ProtoNotification {

    private final short id;


    public ProtoNotification(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

}
