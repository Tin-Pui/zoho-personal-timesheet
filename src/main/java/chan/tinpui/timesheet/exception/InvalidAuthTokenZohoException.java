package chan.tinpui.timesheet.exception;

public class InvalidAuthTokenZohoException extends ZohoException {
    public InvalidAuthTokenZohoException(String message) {
        super(message);
    }

    public InvalidAuthTokenZohoException(Throwable cause) {
        super(cause);
    }

    public InvalidAuthTokenZohoException(String message, Throwable cause) {
        super(message, cause);
    }
}
