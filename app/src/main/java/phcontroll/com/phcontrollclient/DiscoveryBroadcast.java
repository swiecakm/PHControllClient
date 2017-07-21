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

    public void detectListeningServer() throws MessageNotSentException, ServerResponseTimeoutException {
        try {
            BroadcastToAllWLANInterfaces(_settings.getWelcomeMessage().getBytes());
            DatagramPacket serverResponse = getServerResponse();
            _listeningServer = new RemoteServer(serverResponse.getAddress(), serverResponse.getPort());
        } catch (IOException e) {
            Log.e("DiscoveryBroadcast",String.format("Detecting server error: %s", e));
        }
    }

    public RemoteServer getListeningServer() {
        return _listeningServer;
    }

    private void BroadcastToAllWLANInterfaces(byte[] sendData) throws SocketException, MessageNotSentException {
        boolean sent = false;
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if(!(networkInterface.getDisplayName().startsWith("wlan"))) { continue; }
            try {
                broadcastToInterface(sendData, networkInterface);
                sent = true;
            } catch (MessageNotSentException e) {
                Log.d("DiscoveryBroadcast", String.format("Can not send the message for the interface: %1$s because of error: %2$s",
                        networkInterface.getDisplayName(), e));
            }
        }
        if(!sent)
            throw new MessageNotSentException("Broadcast message not sent to any interface");
    }

    private void broadcastToInterface(byte[] sendData, NetworkInterface networkInterface)
            throws MessageNotSentException {
        List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
        Log.d("DiscoveryBroadcast", String.format("%d addresses found", addresses.size()));
        sendMessageForAddresses(sendData, addresses);
    }

    private void sendMessageForAddresses(byte[] sendData, List<InterfaceAddress> addresses) throws MessageNotSentException {
        boolean sent = false;
        Iterator<InterfaceAddress> iterator = addresses.iterator();
        while (iterator.hasNext()) {
            InterfaceAddress address = iterator.next();
            try {
                sendMessageToInterfaceAddress(sendData, address);
                sent = true;
                Log.d("DiscoveryBroadcast", String.format("Broadcast message sent for address %s", address.getAddress()));
            } catch (Exception e) {
                Log.d("DiscoveryBroadcast", String.format("Can not send the message to address %1$s because of error: %2$s", address.getAddress().toString(), e));
            }
        }
        if(!sent)
            throw new MessageNotSentException("Broadcast message not sent to any address");
    }

    private void sendMessageToInterfaceAddress(byte[] sendData, InterfaceAddress address) throws MessageNotSentException {
        try (DatagramSocket c = new DatagramSocket(null)) {
            c.setSoTimeout(_settings.getServerResponseTimeout());
            InetAddress broadcast = address.getBroadcast();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, _settings.getPortNumber());
            c.send(sendPacket);
        }
        catch (Exception e) {
            throw new MessageNotSentException(String.format("Sending broadcast message failed for address %1$s because of error %2$s",
                    address.getAddress(), e));
        }
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
