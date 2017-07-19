package phcontroll.com.phcontrollclient;

import android.os.AsyncTask;

public class ConnectServerTask extends AsyncTask<Void, Void, Void> {
    private OnConnectionCompleted _listener;
    private NetClient _connectedClient;

    public ConnectServerTask(NetClient connectedClient, OnConnectionCompleted listener) {
        _listener = listener;
        _connectedClient = connectedClient;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        _connectedClient.pairWithServer();
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        _listener.onConnectionCompleted();
    }
}