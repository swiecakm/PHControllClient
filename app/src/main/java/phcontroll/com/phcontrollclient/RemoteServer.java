package phcontroll.com.phcontrollclient;

import java.net.InetAddress;

/**
 * Created by root on 18.07.17.
 */
public class RemoteServer {
    private InetAddress _address;

    public InetAddress getAddress() {
        return _address;
    }

    public RemoteServer(InetAddress address){
        _address = address;
    }
}
