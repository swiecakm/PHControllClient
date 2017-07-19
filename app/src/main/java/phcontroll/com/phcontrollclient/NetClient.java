package phcontroll.com.phcontrollclient;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class NetClient {
    private ConnectionSettings _settings = ConnectionSettings.getInstance();
    private RemoteServer _pairedServer;

    public NetClient() {

    }

    public void pairWithServer(){
        try{
            sendBroadcastMessage(_settings.getWelcomeMessage().getBytes());
            DatagramPacket serverResponse = getServerResponse();
            _pairedServer = new RemoteServer(serverResponse.getAddress());
            Log.d("NetClient", String.format("Paired with server with address: %s", getServerAddress()));
        }
        catch (Exception e)
        {
            _pairedServer = null;
            Log.d("NetClient", String.format("Cannot pair with server because of error: %s", e));
        }
    }

    public String getServerAddress() {
        return _pairedServer.getAddress().getHostAddress();
    }


    public void sendMessageToServer(String message) throws NetClientServerNotConnectedException, NetClientBroadcastException {
        if (_pairedServer == null) {
            throw new NetClientServerNotConnectedException("Pair with server before sending message");
        }

        byte[] sentMessage = message.getBytes();
        try (DatagramSocket dSocket = new DatagramSocket(_settings.getPortNumber())) {
            DatagramPacket packet = new DatagramPacket(sentMessage, sentMessage.length, _pairedServer.getAddress(), _settings.getPortNumber());
            dSocket.send(packet);
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of socket error: %s", e));
        } catch (IOException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of IO error: %s", e));
        }
    }

    private void sendBroadcastMessage(byte[] sendData) throws NetClientBroadcastException {
        try {
            BroadcastToAllWLANInterfaces(sendData);
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Cannot get network interfaces: %s", e));
        }
    }

    private void BroadcastToAllWLANInterfaces(byte[] sendData) throws SocketException, NetClientBroadcastException {
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.getDisplayName().startsWith("wlan")) {
                broadcastToAllInterfaceAddresses(sendData, networkInterface);
            }
        }
    }

    private void broadcastToAllInterfaceAddresses(byte[] sendData, NetworkInterface networkInterface)
            throws NetClientBroadcastException {
        try (DatagramSocket c = new DatagramSocket(null)) {
            c.setSoTimeout(_settings.getServerResponseTimeout());
            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            Log.d("NetClient", String.format("%d addresses found", addresses.size()));
            if (!sendMessagesThroughIteratorElements(false, sendData, c, addresses.iterator())) {
                throw new NetClientBroadcastException("Broadcast message not sent to any address");
            }
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Socket initialization exception: %s", e.getMessage()));
        }
    }

    private boolean sendMessagesThroughIteratorElements(boolean atLeastOneSent, byte[] sendData, DatagramSocket c, Iterator<InterfaceAddress> iterator) {
        if (iterator.hasNext()) {
            InterfaceAddress address = iterator.next();
            try {
                sendMessageToInterfaceAddress(sendData, c, address);
                Log.d("NetClient", String.format("Broadcast message sent for address %s", address.getAddress().toString()));
                atLeastOneSent = sendMessagesThroughIteratorElements(true, sendData, c, iterator);
            } catch (Exception e) {
                Log.d("Net Client", String.format("Can not send the message to address %s because of error: %s", address.getAddress().toString(), e));
                atLeastOneSent = sendMessagesThroughIteratorElements(atLeastOneSent, sendData, c, iterator);
            }
        }
        return atLeastOneSent;
    }

    private void sendMessageToInterfaceAddress(byte[] sendData, DatagramSocket c, InterfaceAddress interfaceAddress) throws IOException {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, _settings.getPortNumber());
        c.send(sendPacket);
    }

    private DatagramPacket getServerResponse() throws NetClientServerResponseException {
        try (DatagramSocket c = new DatagramSocket(_settings.getPortNumber())) {
            c.setSoTimeout(_settings.getServerResponseTimeout());
            byte[] respondBuff = new byte[100];
            DatagramPacket packet = new DatagramPacket(respondBuff, respondBuff.length);
            c.receive(packet);
            return packet;
        } catch (InterruptedIOException e) {
            throw new NetClientServerResponseException(String.format("Waiting for server too long: %s", e.getMessage()));
        } catch (IOException e) {
            throw new NetClientServerResponseException(String.format("Receiving server response error: %s", e.getMessage()));
        } catch (Exception e) {
            throw new NetClientServerResponseException(String.format("Receiving server response error: %s", e));
        }
    }

    public boolean isPaired(){
        return _pairedServer != null;
    }
}


