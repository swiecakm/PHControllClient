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
public class BroadcastServer {
    private ConnectionSettings _settings = ConnectionSettings.getInstance();

    public void sendMessage(byte[] sendData) throws NetClientBroadcastException {
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
            Log.d("BroadcastServer", String.format("%d addresses found", addresses.size()));
            if (!sendMessagesForAddresses(false, sendData, c, addresses.iterator())) {
                throw new NetClientBroadcastException("Broadcast message not sent to any address");
            }
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Socket initialization exception: %s", e.getMessage()));
        }
    }

    private boolean sendMessagesForAddresses(boolean atLeastOneSent, byte[] sendData, DatagramSocket c, Iterator<InterfaceAddress> iterator) {
        if (iterator.hasNext()) {
            InterfaceAddress address = iterator.next();
            try {
                sendMessageToInterfaceAddress(sendData, c, address);
                Log.d("BroadcastServer", String.format("Broadcast message sent for address %s", address.getAddress().toString()));
                atLeastOneSent = sendMessagesForAddresses(true, sendData, c, iterator);
            } catch (Exception e) {
                Log.d("BroadcastServer", String.format("Can not send the message to address %s because of error: %s", address.getAddress().toString(), e));
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

    public DatagramPacket getServerResponse() throws NetClientServerResponseException {
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
}
