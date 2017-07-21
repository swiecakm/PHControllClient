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


    public void send(Commands command) throws NotPairedWithServerException, MessageNotSentException {
        if (_pairedServer == null)
            throw new NotPairedWithServerException("Pair with server before sending message");

        sendCommandToPairedServer(command);
    }

    private void sendCommandToPairedServer(Commands command) throws MessageNotSentException {
        try (DatagramSocket dSocket = new DatagramSocket(_pairedServer.getPortNumber())) {
            byte[] sentMessage = command.getBytes();
            DatagramPacket packet = new DatagramPacket(sentMessage, sentMessage.length, _pairedServer.getAddress(), _pairedServer.getPortNumber());
            dSocket.send(packet);
        } catch (SocketException e) {
            throw new MessageNotSentException(String.format("Cannot send message because of socket error: %s", e));
        } catch (IOException e) {
            throw new MessageNotSentException(String.format("Cannot send message because of IO error: %s", e));
        }
    }

    public boolean isPaired(){
        return _pairedServer != null;
    }
}


