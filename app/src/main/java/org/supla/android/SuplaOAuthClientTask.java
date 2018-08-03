package org.supla.android;

import android.os.AsyncTask;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaOAuthToken;

import java.util.Date;

public class SuplaOAuthClientTask extends AsyncTask {

    private SuplaOAuthToken Token;
    private static final String log_tag = "SuplaOAuthClientTask";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Token = SuplaApp.getApp().RegisterOAuthClientTask(this);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        SuplaApp.getApp().UnregisterOAuthClientTask(this);
    }


    public synchronized void setToken(SuplaOAuthToken token) {
        Token = token == null ? null : new SuplaOAuthToken(token);
        notify();
    }

    public synchronized SuplaOAuthToken getTokenWhenIsAlive() {
        return Token != null && Token.isAlive() ? new SuplaOAuthToken(Token) : null;
    }

    private void makeTokenRequest() {
        if (Token != null && Token.isAlive()) return;

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client == null) {
            Trace.d(log_tag, "Client is not available");
            return;
        }


        client.OAuthTokenRequest();

        synchronized (this) {
            try {
                this.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        makeTokenRequest();
        return null;
    }
}
