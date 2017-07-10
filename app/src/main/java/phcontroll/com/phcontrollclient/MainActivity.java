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

        _textServerAddress = (EditText) findViewById(R.id.editText1);
        _textServerAddress.setText("-");

        _volUpButton = (Button) findViewById(R.id.button2);
        _volDownButton = (Button) findViewById(R.id.button3);
        _volUpButton.setEnabled(false);
        _volDownButton.setEnabled(false);
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

        return connectionClient;
    }


    public void onConnectButtonClick(View view) {
        _connectionClient = initializeConnection();
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
}
