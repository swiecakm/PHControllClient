package phcontroll.com.phcontrollclient;

public class ServerResponseTimeoutException extends Exception {
    private static final long serialVersionUID = -1911829663171570739L;

    public ServerResponseTimeoutException(String message) {
        super(message);
    }
}