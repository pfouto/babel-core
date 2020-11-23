package pt.unl.fct.di.novasys.babel.generic;

/**
 * Abstract Request class to be extended by protocol-specific requests.
 */
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
