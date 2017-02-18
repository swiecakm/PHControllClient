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
import android.os.AsyncTask;
import android.util.Log;

public class NetClient extends AsyncTask<Void, Void, Void>
{	
	private String _serverWelcomMessage = "TEST_WELCOME";
	private int _serverResponseTimeoutMs = 10000;
	private int _connectionPortNum = 8888;
	
	@Override
	protected Void doInBackground(Void... arg0) 
	{	
		Log.d("NetClient", "async started");	
		try
		{
			DatagramPacket serverResponse = getServerConnection();
			Log.d("NetClient", "Server response: " + new String(serverResponse.getData()).trim());
			Log.d("NetClient", "Server address: " + serverResponse.getAddress());
		}
		catch (SocketException e)
		{
			Log.d("NetClient", "Find sever exception: Problem with socket: " + e.getMessage());
		}
		catch (NetClientBroadcastException e)
		{
			Log.d("NetClient", "Broadcast to all addresses exception: " + e.getMessage());
		}
		catch (NetClientServerResponseException e)
		{
			Log.d("NetClient", "Getting server response exception: " + e.getMessage());
		}
		catch (UnknownHostException e)
		{
			Log.d("NetClient", "Getting server response exception: " + e.getMessage());
		}
		return null;
	}

	private DatagramPacket getServerConnection() throws NetClientBroadcastException, NetClientServerResponseException, SocketException, UnknownHostException 
	{
		sendBroadcastMessage(_serverWelcomMessage.getBytes());
		DatagramPacket serverResponse = getServerResponse();
		
		return serverResponse;
	}

	private void sendBroadcastMessage( byte[] sendData) throws NetClientBroadcastException
	{
		try
		{
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
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
		try
		{
			DatagramSocket c = new DatagramSocket(null);
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
		DatagramPacket packet;
		try
		{
			DatagramSocket c = new DatagramSocket(_connectionPortNum);
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
		
		return packet;
	}
}

	

