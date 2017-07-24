package phcontroll.com.phcontrollclient;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements OnConnectionCompleted {
    private CommandsSendingClient _connectionClient = null;
    private ImageButton _volUpButton;
    private ImageButton _volDownButton;
    private Button _connectButton;
    private EditText _serverAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeUIComponents();
        _connectionClient = new CommandsSendingClient();
    }

    private void InitializeUIComponents() {
        retrieveWidgets();
        setWidgetsInitialValues();
    }

    private void retrieveWidgets() {
        _serverAddressText = (EditText) findViewById(R.id.serverAddressText);
        _volUpButton = (ImageButton) findViewById(R.id.volUpButton);
        _volDownButton = (ImageButton) findViewById(R.id.volDownButton);
        _connectButton = (Button) findViewById(R.id.connectButton);
    }

    private void setWidgetsInitialValues() {
        _serverAddressText.setKeyListener(null);
        _volUpButton.setEnabled(false);
        _volDownButton.setEnabled(false);
        _serverAddressText.setText("Click to connect with server");
    }

    public void onVolUpButtonClick(View view) {
        sendCommandToServer(Commands.VOL_UP);
    }

    public void onVolDownButtonClick(View view){
        sendCommandToServer(Commands.VOL_DOWN);
    }

    public void onMuteButtonClick(View view) { sendCommandToServer(Commands.MUTE); }

    public void onPlayPauseButtonClick(View view) { sendCommandToServer(Commands.PLAY_PAUSE); }

    public void onStopButtonClick(View view) { sendCommandToServer(Commands.STOP); }

    public void onPreviousButtonClick(View view) { sendCommandToServer(Commands.PREVIOUS); }

    public void onNextButtonClick(View view) { sendCommandToServer(Commands.NEXT); }
    
    public void sendCommandToServer(Commands command) {
        if (_connectionClient.isPaired()) {
            SendMessageTask sendTask = new SendMessageTask(_connectionClient, command);
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
        ConnectServerTask connectionTask = new ConnectServerTask(_connectionClient, this);
        connectionTask.execute();
    }

    @Override
    public void onConnectionCompleted() {
        if (_connectionClient.isPaired()) {
            SetConnectionStatus(String.format("Connected with IP: %s", _connectionClient.getServerAddress()), true);
        } else {
            SetConnectionStatus("Could not connect. Please try again.", false);
        }
        _connectButton.setEnabled(true);
    }

    private void SetConnectionStatus(String message, Boolean volumeButtonsEnabled) {
        _serverAddressText.setText(message);
        _volUpButton.setEnabled(volumeButtonsEnabled);
        _volDownButton.setEnabled(volumeButtonsEnabled);
    }
}
