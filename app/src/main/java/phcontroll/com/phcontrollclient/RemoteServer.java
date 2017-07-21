package phcontroll.com.phcontrollclient;

import java.net.InetAddress;

/**
 * Created by root on 18.07.17.
 */
public class RemoteServer {
    private InetAddress _address;
    private int _port;

    public InetAddress getAddress() {
        return _address;
    }
    public int getPortNumber() { return _port; }

    public RemoteServer(InetAddress address, int port){
        _address = address;
        _port = port;
    }

}
