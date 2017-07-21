package phcontroll.com.phcontrollclient;

public class NotPairedWithServerException extends Exception {
    private static final long serialVersionUID = -1544705844993870235L;

    public NotPairedWithServerException(String message) {
        super(message);
    }
}