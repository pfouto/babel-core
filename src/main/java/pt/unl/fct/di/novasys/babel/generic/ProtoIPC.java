package pt.unl.fct.di.novasys.babel.generic;

public abstract class ProtoIPC {

    public enum Type { REPLY, REQUEST}

    private Type type;

    public ProtoIPC(Type t){
        this.type = t;
    }

    public Type getType() {
        return type;
    }
}
