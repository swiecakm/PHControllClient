package phcontroll.com.phcontrollclient;

import android.os.AsyncTask;
import android.util.Log;

public class SendMessageTask extends AsyncTask<Void, Void, Void> {
    private CommandsSendingClient _connectionClient;
    private Commands _command;

    public SendMessageTask(CommandsSendingClient connectionClient, Commands command) {
        _connectionClient = connectionClient;
        _command = command;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            _connectionClient.send(_command);
        } catch (Exception e) {
            Log.d("SendMessageTask", "Cannot send message!: " + e);
        }
        return null;
    }
}