package phcontroll.com.phcontrollclient;

/**
 * Created by root on 18.07.17.
 */
public class ConnectionSettings {
    private final String _welcomeMessage = "TEST_WELCOME";
    private final int _serverResponseTimeoutMs = 10000;
    private final int _portNumber = 8888;

    public static ConnectionSettings getInstance(){
        return ConnectionSettingsHolder.INSTANCE;
    }

    public int getPortNumber() {
        return _portNumber;
    }

    public String getWelcomeMessage() {
        return _welcomeMessage;
    }

    public int getServerResponseTimeout() {
        return _serverResponseTimeoutMs;
    }


    private static class ConnectionSettingsHolder {
        private static ConnectionSettings INSTANCE = new ConnectionSettings();
    }
}
