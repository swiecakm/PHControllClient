package com.example.phcontrollclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import android.util.Log;

public class NetClient
{	
	private String _serverWelcomMessage = "TEST_WELCOME";
	private int _serverResponseTimeoutMs = 10000;
	private int _connectionPortNum;
	private InetAddress _connectionServerAddress;
	
	public NetClient(int portNum)
	{
		_connectionPortNum = portNum;
	}
	
	public void initialize() throws NetClientBroadcastException, NetClientServerResponseException 
	{
		sendBroadcastMessage(_serverWelcomMessage.getBytes());
		DatagramPacket serverResponse = getServerResponse();
		_connectionServerAddress = serverResponse.getAddress();
	}
	
	public String getServerAddress()
	{
		return _connectionServerAddress.getHostAddress();
	}
	
	
	public void sendMessageToServer(String message) throws Exception
	{
		if(_connectionServerAddress == null)
		{
			throw new NetClientServerNotConnectedException("Initialize connection before sending message");
		}
		
		byte[] sentMessage = message.getBytes();
		DatagramPacket packet = new DatagramPacket(sentMessage, sentMessage.length,
				_connectionServerAddress, _connectionPortNum);

		DatagramSocket dsocket = null;
		try
		{
			dsocket = new DatagramSocket(_connectionPortNum);
			dsocket.send(packet);
		}
		catch (SocketException e)
		{
			throw new Exception("Cannot send packet: " + e);
		}
		catch (IOException e)
		{
			throw new Exception("Cannot send packet: " + e);
		}
		finally
		{
			if(dsocket != null)
			{
				dsocket.close();
			}
		}
	}

	private void sendBroadcastMessage( byte[] sendData) throws NetClientBroadcastException
	{
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				if (!networkInterface.getDisplayName().startsWith("wlan"))
				{
					continue;
				}		
				if (!networkInterface.isUp())
				{
					throw new NetClientBroadcastException("Wifi is not enabled");
				}			
				broadcastToAllInterfaceAddresses(sendData, networkInterface);
			}
		}
		catch (SocketException e)
		{
			throw new NetClientBroadcastException("Cannot get network interfaces: " + e.getMessage());
		}
	}

	private void broadcastToAllInterfaceAddresses(byte[] sendData, NetworkInterface networkInterface)
			throws NetClientBroadcastException
	{
		DatagramSocket c = null;
		try
		{
			c = new DatagramSocket(null);
			c.setSoTimeout(_serverResponseTimeoutMs);
			
			boolean messageSent = false;
			for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
			{			
				if(sendMessageToInterfaceAddress(sendData, c, messageSent, interfaceAddress))
				{
					messageSent = true;
				}
			}
			if (!messageSent)
			{
				throw new NetClientBroadcastException("Broadcast message not sent to any address");
			}
		}
		catch (SocketException e)
		{
			throw new NetClientBroadcastException("Creating socket exception: " + e.getMessage());
		}
		finally
		{
			if(c != null)
			{
				c.close();
			}
		}
	}

	private boolean sendMessageToInterfaceAddress(byte[] sendData, DatagramSocket c, boolean messageSent,
			InterfaceAddress interfaceAddress)
	{
		try
		{
			InetAddress broadcast = interfaceAddress.getBroadcast();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,broadcast, _connectionPortNum);
			c.send(sendPacket);		
			Log.d("NetClient", "Broadcast message sent for address " + interfaceAddress.getAddress().toString());
			
			return true;
		}
		catch (Exception e)
		{
			Log.d("NetClient", "Sending broadcast package error for address " + interfaceAddress.getAddress().toString() + 
									": " + e.getMessage());
			return false;
		}
	}
	
	private DatagramPacket getServerResponse() throws NetClientServerResponseException
	{
		DatagramSocket c = null;
		DatagramPacket packet;
		try
		{
			c = new DatagramSocket(_connectionPortNum);
			c.setSoTimeout(_serverResponseTimeoutMs);
			
			byte[] respondBuff = new byte[100];
			packet = new DatagramPacket(respondBuff, respondBuff.length);
			c.receive(packet);	
		}
		catch(InterruptedIOException e)
		{
			throw new NetClientServerResponseException("Waiting for server too long: " + e.getMessage());
		}
		catch(IOException e)
		{
			throw new NetClientServerResponseException("Receiving server response error: " + e.getMessage());
		}
		finally
		{
			if(c != null)
			{
				c.close();
			}
		}
		
		return packet;
	}
}

	


