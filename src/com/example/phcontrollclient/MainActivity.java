package com.example.phcontrollclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;

import com.example.phcontrollclient.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		EditText textFirstName = (EditText) findViewById(R.id.editText1);
		textFirstName.setText("Welcome!");
		
		InetAddress serverAddress = null;
		NetClient phClient = new NetClient();
		phClient.execute();
		try
		{
			serverAddress = phClient.get();
		}
		catch (InterruptedException e)
		{
			Log.d("MainActivity","Cannot find server address because thread was interrupted: " + e.getMessage());
		}
		catch (ExecutionException e)
		{
			Log.d("MainActivity","Cannot find server address: " + e.getMessage());
		}
		
		if(serverAddress != null)
		{
			Log.d("MainActivity","Server address found: " + serverAddress);
			textFirstName.setText("Server address: " + serverAddress.toString().trim());
		}
		else
		{
			Log.d("MainActivity","Server address not found!");
		}
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
}
