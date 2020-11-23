package pt.unl.fct.di.novasys.babel.generic;

/**
 * Abstract Notification class to be extended by protocol-specific notifications.
 */
public abstract class ProtoNotification {

    private final short id;


    public ProtoNotification(short id){
        this.id = id;
    }

    public short getId() {
        return id;
    }

}
