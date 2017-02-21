package com.example.phcontrollclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import com.example.phcontrollclient.R;

public class MainActivity extends Activity {

	private NetClient connectionClient = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		EditText textFirstName = (EditText) findViewById(R.id.editText1);
		textFirstName.setText("-");	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onConnectButtonClick(View view)
	{
		EditText textFirstName = (EditText) findViewById(R.id.editText1);
		
		ConnectServerTask connectionTask = new ConnectServerTask();
		connectionTask.execute();
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
		
		if(connectionClient != null)
		{
			Log.d("MainActivity","Connected with server with address: " + connectionClient.getServerAddress());
			textFirstName.setText(connectionClient.getServerAddress());
		}
		else
		{
			Log.d("MainActivity","Server address not found!");
		}
	}
}
