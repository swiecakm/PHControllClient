package phcontroll.com.phcontrollclient;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements OnConnectionCompleted {
    private NetClient _connectionClient = null;
    private Button _volUpButton;
    private Button _volDownButton;
    private Button _connectButton;
    private EditText _serverAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeUIComponents();
    }

    private void InitializeUIComponents() {
        retrieveWidgets();
        setWidgetsInitialValues();
    }

    private void retrieveWidgets() {
        _serverAddressText = (EditText) findViewById(R.id.serverAddressText);
        _volUpButton = (Button) findViewById(R.id.volUpButton);
        _volDownButton = (Button) findViewById(R.id.volDownButton);
        _connectButton = (Button) findViewById(R.id.connectButton);
    }

    private void setWidgetsInitialValues() {
        _serverAddressText.setKeyListener(null);
        _volUpButton.setEnabled(false);
        _volDownButton.setEnabled(false);
        _serverAddressText.setText("Click to connect with server");
    }

    public void onVolUpButtonClick(View view) {
        sendCommandToServer("UP");
    }

    public void onVolDownButtonClick(View view) {
        sendCommandToServer("DOWN");
    }

    public void sendCommandToServer(String message) {
        if (_connectionClient != null) {
            SendMessageTask sendTask = new SendMessageTask(_connectionClient, message);
            sendTask.execute();
        } else
            Log.d("MainActivity", "Connect with server before sending command!");
    }

    public void onConnectButtonClick(View view) {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            SetConnectionStatus("Please enable wifi and try again.", false);
        } else {
            initializeServerConnection();
        }
    }

    private void initializeServerConnection() {
        _serverAddressText.setText("Connecting with server...");
        _connectButton.setEnabled(false);
        ConnectServerTask connectionTask = new ConnectServerTask(this);
        connectionTask.execute();
    }

    @Override
    public void onConnectionCompleted(ConnectServerTaskResult result) {
        if (result.isConnected()) {
            _connectionClient = result.getServerConnection();
            SetConnectionStatus(String.format("Connected with IP: %s", _connectionClient.getServerAddress()), true);
            Log.d("MainActivity", String.format("Connected with server with address: %s", _connectionClient.getServerAddress()));
        } else {
            SetConnectionStatus("Could not connect. Please try again.", false);
            Log.d("MainActivity", String.format("Not connected with server because of error: %s", result.getConnectionException()));
        }
        _connectButton.setEnabled(true);
    }

    private void SetConnectionStatus(String message, Boolean volumeButtonsEnabled) {
        _serverAddressText.setText(message);
        _volUpButton.setEnabled(volumeButtonsEnabled);
        _volDownButton.setEnabled(volumeButtonsEnabled);
    }

}
