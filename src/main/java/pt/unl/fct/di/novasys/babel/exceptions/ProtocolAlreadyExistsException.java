package pt.unl.fct.di.novasys.babel.exceptions;

public class ProtocolAlreadyExistsException extends Exception {

    public ProtocolAlreadyExistsException() {
    }

    public ProtocolAlreadyExistsException(String message) {
        super(message);
    }

    public ProtocolAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public ProtocolAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
