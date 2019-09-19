package babel.exceptions;

public class NotificationDoesNotExistException extends Exception {

    public NotificationDoesNotExistException() {
    }

    public NotificationDoesNotExistException(String message) {
        super(message);
    }

    public NotificationDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public NotificationDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
