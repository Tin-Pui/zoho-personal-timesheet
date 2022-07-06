package chan.tinpui.timesheet.exception;

public class ZohoException extends Exception {
    public ZohoException(String message) {
        super(message);
    }

    public ZohoException(Throwable cause) {
        super(cause);
    }

    public ZohoException(String message, Throwable cause) {
        super(message, cause);
    }
}
