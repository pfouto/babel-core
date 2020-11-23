package pt.unl.fct.di.novasys.babel.generic;

/**
 * Abstract Reply class to be extended by protocol-specific requests.
 */
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
