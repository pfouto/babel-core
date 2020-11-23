package pt.unl.fct.di.novasys.babel.generic;

/**
 * Abstract Timer class to be extended by protocol-specific timers.
 */
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
