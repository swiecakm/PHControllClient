package com.example.phcontrollclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.concurrent.ExecutionException;

import com.example.phcontrollclient.R;

public class MainActivity extends Activity
{
	private NetClient _connectionClient = null;
	private Button _volUpButton;
	private Button _volDownButton;
	private EditText _textServerAddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_textServerAddress = (EditText) findViewById(R.id.editText1);
		_textServerAddress.setText("-");	

		_volUpButton = (Button) findViewById(R.id.button2);
		_volDownButton = (Button) findViewById(R.id.button3);
		_volUpButton.setEnabled(false);
		_volDownButton.setEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onVolUpButtonClick(View view)
	{
		SendMessageTask sendTask = new SendMessageTask(_connectionClient,"UP");
		sendTask.execute();
	}
	
	public void onVolDownButtonClick(View view)
	{
		SendMessageTask sendTask = new SendMessageTask(_connectionClient,"DOWN");
		sendTask.execute();
	}
		
	public void onConnectButtonClick(View view)
	{
		_connectionClient = ConnectWithServer();	
		if(_connectionClient != null)
		{
			Log.d("MainActivity","Connected with server with address: " + _connectionClient.getServerAddress());
			_textServerAddress.setText(_connectionClient.getServerAddress());
			
			_volUpButton.setEnabled(true);
			_volDownButton.setEnabled(true);
		}
		else
		{
			Log.d("MainActivity","Server address not found!");
		}
		
	}

	private NetClient ConnectWithServer()
	{
		ConnectServerTask connectionTask = new ConnectServerTask();
		connectionTask.execute();
		NetClient connectionClient = null;
		try
		{
			connectionClient = connectionTask.get();
		}
		catch (InterruptedException e)
		{
			Log.d("MainActivity","Cannot find server address because thread was interrupted: " + e.getMessage());
		}
		catch (ExecutionException e)
		{
			Log.d("MainActivity","Cannot find server address: " + e.getMessage());
		}
		
		return connectionClient;
	}
}
