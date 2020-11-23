package pt.unl.fct.di.novasys.babel.exceptions;

public class HandlerRegistrationException extends Exception {

    public HandlerRegistrationException() {
    }

    public HandlerRegistrationException(String message) {
        super(message);
    }

    public HandlerRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerRegistrationException(Throwable cause) {
        super(cause);
    }

    public HandlerRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
