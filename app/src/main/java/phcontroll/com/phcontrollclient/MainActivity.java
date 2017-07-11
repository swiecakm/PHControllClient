package phcontroll.com.phcontrollclient;

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

        _textServerAddress = (EditText) findViewById(R.id.serverAddressText);
        _textServerAddress.setText("-");

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
            Log.d("MainActivity", "No server to send a command!");
    }

    public void onConnectButtonClick(View view) {
        _connectionClient = initializeConnection();
        if(_connectionClient != null)
        {
            Log.d("MainActivity", String.format("Connected with server with address: %s", _connectionClient.getServerAddress()));
            _textServerAddress.setText(_connectionClient.getServerAddress());

            _volUpButton.setEnabled(true);
            _volDownButton.setEnabled(true);
        }
        else
        {
            Log.d("MainActivity","Server address not found!");
            _textServerAddress.setText("Server address not found!");
        }

    }

    private NetClient initializeConnection()
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
        catch (Exception e)
        {
            Log.d("MainActivity", String.format("Cannot initialize connection because of error: %s", e));
        }

        return connectionClient;
    }


}
