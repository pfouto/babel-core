package babel.exceptions;

public class DestinationProtocolDoesNotExist extends Exception {
    public DestinationProtocolDoesNotExist() {
    }

    public DestinationProtocolDoesNotExist(String message) {
        super(message);
    }

    public DestinationProtocolDoesNotExist(String message, Throwable cause) {
        super(message, cause);
    }

    public DestinationProtocolDoesNotExist(Throwable cause) {
        super(cause);
    }

    public DestinationProtocolDoesNotExist(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
