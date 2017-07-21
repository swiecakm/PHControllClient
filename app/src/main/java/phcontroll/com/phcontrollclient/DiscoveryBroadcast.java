package phcontroll.com.phcontrollclient;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static java.net.NetworkInterface.getNetworkInterfaces;

/**
 * Created by root on 21.07.17.
 */
public class DiscoveryBroadcast {
    private ConnectionSettings _settings = ConnectionSettings.getInstance();
    private RemoteServer _listeningServer;

    public void detectListeningServer() throws SendMessageException, ServerResponseTimeoutException {
        try {
            sendMessage(_settings.getWelcomeMessage().getBytes());
            DatagramPacket serverResponse = getServerResponse();
            _listeningServer = new RemoteServer(serverResponse.getAddress(), serverResponse.getPort());
        } catch (IOException e) {
            Log.e("DiscoveryBroadcast",String.format("Detecting server error: %s", e));
        }
    }

    public RemoteServer getListeningServer() {
        return _listeningServer;
    }

    private void sendMessage(byte[] sendData) throws SendMessageException, SocketException {
        BroadcastToAllWLANInterfaces(sendData);
    }

    private void BroadcastToAllWLANInterfaces(byte[] sendData) throws SocketException, SendMessageException {
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.getDisplayName().startsWith("wlan")) {
                broadcastToAllInterfaceAddresses(sendData, networkInterface);
            }
        }
    }

    private void broadcastToAllInterfaceAddresses(byte[] sendData, NetworkInterface networkInterface)
            throws SendMessageException {
        try (DatagramSocket c = new DatagramSocket(null)) {
            c.setSoTimeout(_settings.getServerResponseTimeout());
            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            Log.d("BroadcastServerr", String.format("%d addresses found", addresses.size()));
            if (!sendMessagesForAddresses(false, sendData, c, addresses.iterator())) {
                throw new SendMessageException("Broadcast message not sent to any address");
            }
        } catch (SocketException e) {
            Log.e("DiscoveryBroadcast", String.format("Broadcast not sent for interface: %1$s because of socket error: $2$s",
                    networkInterface, e));
        }
    }

    private boolean sendMessagesForAddresses(boolean atLeastOneSent, byte[] sendData, DatagramSocket c, Iterator<InterfaceAddress> iterator) {
        if (iterator.hasNext()) {
            InterfaceAddress address = iterator.next();
            try {
                sendMessageToInterfaceAddress(sendData, c, address);
                Log.d("BroadcastServerr", String.format("Broadcast message sent for address %s", address.getAddress().toString()));
                atLeastOneSent = sendMessagesForAddresses(true, sendData, c, iterator);
            } catch (Exception e) {
                Log.d("BroadcastServerr", String.format("Can not send the message to address %1$s because of error: $2$s", address.getAddress().toString(), e));
                atLeastOneSent = sendMessagesForAddresses(atLeastOneSent, sendData, c, iterator);
            }
        }
        return atLeastOneSent;
    }

    private void sendMessageToInterfaceAddress(byte[] sendData, DatagramSocket c, InterfaceAddress interfaceAddress) throws IOException {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, _settings.getPortNumber());
        c.send(sendPacket);
    }

    private DatagramPacket getServerResponse() throws ServerResponseTimeoutException, IOException {
        try (DatagramSocket c = new DatagramSocket(_settings.getPortNumber())) {
            c.setSoTimeout(_settings.getServerResponseTimeout());
            byte[] respondBuff = new byte[100];
            DatagramPacket packet = new DatagramPacket(respondBuff, respondBuff.length);
            c.receive(packet);
            return packet;
        } catch (InterruptedIOException e) {
            throw new ServerResponseTimeoutException(String.format("Waiting for server too long: %s", e));
        }
    }

}
