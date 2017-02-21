package com.example.phcontrollclient;

import java.net.InetAddress;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectServerTask extends AsyncTask<Void, Void, NetClient>
{

	private int _connectionPortNum = 8888;
	
	@Override
	protected NetClient doInBackground(Void... arg0) 
	{	
		Log.d("NetClient", "async started");	
		NetClient connectionClient = null;
		try
		{
			connectionClient = new NetClient(_connectionPortNum);
			connectionClient.initialize();
			Log.d("NetClient", "Server address: " + connectionClient.getServerAddress());
		}
		catch (NetClientBroadcastException e)
		{
			Log.d("NetClient", "Broadcast to all addresses exception: " + e.getMessage());
		}
		catch (NetClientServerResponseException e)
		{
			Log.d("NetClient", "Getting server response exception: " + e.getMessage());
		}
		
		return connectionClient;
	}

	@Override
	protected void onPostExecute(NetClient result) {
	    super.onPostExecute(result);
	}
}
