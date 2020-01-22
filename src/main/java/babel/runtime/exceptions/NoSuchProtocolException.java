package babel.runtime.exceptions;

public class NoSuchProtocolException extends Exception {

    public NoSuchProtocolException(short protoId) { super(protoId + " not executing.");}
    public NoSuchProtocolException(short protoId, Throwable cause) { super(protoId + " not executing.", cause);}

    public NoSuchProtocolException(short protoId, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(protoId + " not executing.", cause, enableSuppression, writableStackTrace);
    }
}
