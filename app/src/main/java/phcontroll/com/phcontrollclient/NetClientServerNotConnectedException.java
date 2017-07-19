package phcontroll.com.phcontrollclient;

public class NetClientServerNotConnectedException extends Exception {
    private static final long serialVersionUID = -1544705844993870235L;

    public NetClientServerNotConnectedException(String message) {
        super(message);
    }
}