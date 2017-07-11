package phcontroll.com.phcontrollclient;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private NetClient _connectionClient = null;
    private Button _volUpButton;
    private Button _volDownButton;
    private EditText _textServerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitializeUIComponents();
    }

    private void InitializeUIComponents() {
        _textServerAddress = (EditText) findViewById(R.id.serverAddressText);
        _textServerAddress.setText("Not connected with server");
        _volUpButton = (Button) findViewById(R.id.volUpButton);
        _volDownButton = (Button) findViewById(R.id.volDownButton);
        _volUpButton.setEnabled(false);
        _volDownButton.setEnabled(false);
    }

    public void onVolUpButtonClick(View view)
    {
        sendCommandToServer("UP");
    }

    public void onVolDownButtonClick(View view)
    {
        sendCommandToServer("DOWN");
    }

    public void sendCommandToServer(String message)
    {
        if(_connectionClient !=null) {
            SendMessageTask sendTask = new SendMessageTask(_connectionClient, message);
            sendTask.execute();
        }
        else
            Log.d("MainActivity", "Connect with server before sending command!");
    }

    public void onConnectButtonClick(View view) {
        try{
            WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            if (!wifi.isWifiEnabled()){
                _textServerAddress.setText("Wifi is not enabled!");
                throw new Exception("Wifi is not enabled!");
            }
            ConnectWithServer();
        }
        catch (InterruptedException e)
        {
            Log.d("MainActivity","Cannot find server address because thread was interrupted: " + e.getMessage());
        }
        catch (Exception e)
        {
            Log.d("MainActivity", String.format("Cannot initialize connection because of error: %s", e));
        }
    }

    private void ConnectWithServer() throws Exception {
        ConnectServerTaskResult connectionResult = initializeConnection();
        if(connectionResult.isConnected())
        {
            _connectionClient = connectionResult.getServerConnection();
            Log.d("MainActivity", String.format("Connected with server with address: %s", _connectionClient.getServerAddress()));
            _textServerAddress.setText(_connectionClient.getServerAddress());
            _volUpButton.setEnabled(true);
            _volDownButton.setEnabled(true);
        }
        else
            throw connectionResult.getConnectionException();
    }

    private ConnectServerTaskResult initializeConnection() throws ExecutionException, InterruptedException {
        ConnectServerTask connectionTask = new ConnectServerTask();
        connectionTask.execute();
        return connectionTask.get();
    }


}
