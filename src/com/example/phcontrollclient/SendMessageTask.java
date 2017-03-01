package com.example.phcontrollclient;

import android.os.AsyncTask;
import android.util.Log;

public class SendMessageTask extends AsyncTask<Void, Void, Void>
{
	private NetClient _connectionClient;
	private String _sendMessage;
	
	public SendMessageTask(NetClient connectionClient, String message)
	{
		_connectionClient = connectionClient;
		_sendMessage = message;
	}

	@Override
	protected Void doInBackground(Void... arg0) 
	{
		try
		{
			_connectionClient.sendMessageToServer(_sendMessage);
		}
		catch (Exception e)
		{
			Log.d("MainActivity","Cannot send message!: " + e);
		}
		return null;	
	}
}
