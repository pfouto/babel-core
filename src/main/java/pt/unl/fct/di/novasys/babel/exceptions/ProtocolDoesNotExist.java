package pt.unl.fct.di.novasys.babel.exceptions;

public class ProtocolDoesNotExist extends Exception {
    public ProtocolDoesNotExist() {
    }

    public ProtocolDoesNotExist(String message) {
        super(message);
    }

    public ProtocolDoesNotExist(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolDoesNotExist(Throwable cause) {
        super(cause);
    }

    public ProtocolDoesNotExist(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
