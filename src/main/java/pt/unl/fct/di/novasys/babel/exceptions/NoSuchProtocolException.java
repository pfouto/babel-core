package pt.unl.fct.di.novasys.babel.exceptions;

public class NoSuchProtocolException extends RuntimeException {

    public NoSuchProtocolException(short protoId) { super(protoId + " not executing.");}
    public NoSuchProtocolException(String error) { super(error);}
    public NoSuchProtocolException(short protoId, Throwable cause) { super(protoId + " not executing.", cause);}

    public NoSuchProtocolException(short protoId, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(protoId + " not executing.", cause, enableSuppression, writableStackTrace);
    }
}
