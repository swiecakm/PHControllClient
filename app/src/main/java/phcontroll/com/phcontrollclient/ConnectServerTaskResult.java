package phcontroll.com.phcontrollclient;

/**
 * Created by root on 11.07.17.
 */
public class ConnectServerTaskResult {
    private boolean _connected;
    private NetClient _serverConnection;
    private Exception _connectionException;

    public ConnectServerTaskResult(NetClient connection) {
        _connected = true;
        _serverConnection = connection;
    }

    public ConnectServerTaskResult(Exception e) {
        _connected = false;
        _connectionException = e;
    }

    public boolean isConnected() {
        return _connected;
    }

    public NetClient getServerConnection() {
        if (_serverConnection != null) {
            return _serverConnection;
        } else {
            throw new IllegalStateException("Server connection is null!");
        }
    }

    public Exception getConnectionException() {
        if (_connectionException != null) {
            return _connectionException;
        } else {
            throw new IllegalStateException("Connection exception is null!");
        }
    }
}
