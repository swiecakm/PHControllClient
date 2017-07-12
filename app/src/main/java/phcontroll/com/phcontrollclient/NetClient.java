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
    private String _serverWelcomeMessage = "TEST_WELCOME";
    private int _serverResponseTimeoutMs = 10000;
    private int _connectionPortNum;
    private InetAddress _connectionServerAddress;

    public NetClient(int portNum) {
        _connectionPortNum = portNum;
    }

    public void initialize() throws Exception {
        sendBroadcastMessage(_serverWelcomeMessage.getBytes());
        DatagramPacket serverResponse = getServerResponse();
        _connectionServerAddress = serverResponse.getAddress();
    }

    public String getServerAddress() {
        return _connectionServerAddress.getHostAddress();
    }


    public void sendMessageToServer(String message) throws NetClientServerNotConnectedException, NetClientBroadcastException {
        if (_connectionServerAddress == null) {
            throw new NetClientServerNotConnectedException("Initialize connection before sending message");
        }

        byte[] sentMessage = message.getBytes();
        try (DatagramSocket dSocket = new DatagramSocket(_connectionPortNum)) {
            DatagramPacket packet = new DatagramPacket(sentMessage, sentMessage.length, _connectionServerAddress, _connectionPortNum);
            dSocket.send(packet);
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of socket error: %s", e));
        } catch (IOException e) {
            throw new NetClientBroadcastException(String.format("Cannot send packet because of IO error: %s", e));
        }
    }

    private void sendBroadcastMessage(byte[] sendData) throws NetClientBroadcastException {
        try {
            BroadcastToAllWlanInterfaces(sendData);
        } catch (SocketException e) {
            throw new NetClientBroadcastException(String.format("Cannot get network interfaces: %s", e));
        }
    }

    private void BroadcastToAllWlanInterfaces(byte[] sendData) throws SocketException, NetClientBroadcastException {
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
            c.setSoTimeout(_serverResponseTimeoutMs);
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
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, _connectionPortNum);
        c.send(sendPacket);
    }

    private DatagramPacket getServerResponse() throws NetClientServerResponseException {
        try (DatagramSocket c = new DatagramSocket(_connectionPortNum)) {
            c.setSoTimeout(_serverResponseTimeoutMs);
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


