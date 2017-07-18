package phcontroll.com.phcontrollclient;

import android.os.AsyncTask;

public class ConnectServerTask extends AsyncTask<Void, Void, ConnectServerTaskResult> {
    private OnConnectionCompleted _listener;

    public ConnectServerTask(OnConnectionCompleted listener) {
        _listener = listener;
    }

    @Override
    protected ConnectServerTaskResult doInBackground(Void... arg0) {
        try {
            NetClient connectionClient = new NetClient();
            connectionClient.pairWithServer();
            return new ConnectServerTaskResult(connectionClient);
        } catch (Exception e) {
            return new ConnectServerTaskResult(e);
        }
    }

    @Override
    protected void onPostExecute(ConnectServerTaskResult result) {
        _listener.onConnectionCompleted(result);
    }
}