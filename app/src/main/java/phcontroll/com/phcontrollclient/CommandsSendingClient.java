package phcontroll.com.phcontrollclient;

import android.util.Log;

import java.io.IOException;
import java.net.*;

public class CommandsSendingClient {
    private RemoteServer _pairedServer;

    public void pairWithServer(){
        try{
            DiscoveryBroadcast broadcast = new DiscoveryBroadcast();
            broadcast.detectListeningServer();
            _pairedServer = broadcast.getListeningServer();
            Log.d("CommandsSendingClient", String.format("Paired with server with address: %s", getServerAddress()));
        }
        catch (Exception e){
            _pairedServer = null;
            Log.d("CommandsSendingClient", String.format("Cannot pair with server because of error: %s", e));
        }
    }

    public String getServerAddress() {
        return _pairedServer.getAddress().getHostAddress();
    }


    public void send(String message) throws NetClientServerNotConnectedException, NetClientBroadcastException {
        if (_pairedServer == null)
            throw new NetClientServerNotConnectedException("Pair with server before sending message");

        sendCommandToPairedServer(message);
    }

    private void sendCommandToPairedServer(String message) throws NetClientBroadcastException {
        try (DatagramSocket dSocket = new DatagramSocket(_pairedServer.getPortNumber())) {
            byte[] sentMessage = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sentMessage, sentMessage.length, _pairedServer.getAddress(), _pairedServer.getPortNumber());
            dSocket.send(packet);
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of socket error: %s", e));
        } catch (IOException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of IO error: %s", e));
        }
    }

    public boolean isPaired(){
        return _pairedServer != null;
    }
}


