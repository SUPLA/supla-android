package org.supla.android.restapi;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;

import org.json.JSONTokener;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.db.DbHelper;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaOAuthToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public abstract class SuplaRestApiClientTask extends AsyncTask {

    protected static final String log_tag = "SuplaRestApiClientTask";
    private Context _context;
    private int ChannelId = 0;
    private long ActivityTime = 0;
    private SuplaOAuthToken mToken;
    private MeasurementsDbHelper MDbH = null;
    private DbHelper DbH = null;
    private IAsyncResults delegate;

    public SuplaRestApiClientTask(Context context) {
        keepAlive();
        _context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        setToken(SuplaApp.getApp().RegisterRestApiClientTask(this));

        if (delegate != null) {
            delegate.onRestApiTaskStarted(this);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        SuplaApp.getApp().UnregisterRestApiClientTask(this);

        if (delegate != null) {
            delegate.onRestApiTaskFinished(this);
        }
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);

        if (delegate != null
                && values != null
                && values.length > 0
                && values[0] instanceof Double) {

            delegate.onRestApiTaskProgressUpdate(this, (Double) values[0]);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        SuplaApp.getApp().UnregisterRestApiClientTask(this);
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public IAsyncResults getDelegate() {
        return delegate;
    }

    public void setDelegate(IAsyncResults delegate) {
        this.delegate = delegate;
    }

    public synchronized SuplaOAuthToken getToken() {
        return new SuplaOAuthToken(mToken);
    }

    public synchronized void setToken(SuplaOAuthToken token) {
        mToken = token == null ? null : new SuplaOAuthToken(token);
        notify();
    }

    public synchronized SuplaOAuthToken getTokenWhenIsAlive() {
        return mToken != null && mToken.isAlive() ? getToken() : null;
    }

    public synchronized boolean isAlive(int timeout) {
        return isCancelled() && ActivityTime - (System.currentTimeMillis() / 1000L) < timeout;
    }

    public synchronized void keepAlive() {
        ActivityTime = System.currentTimeMillis() / 1000L;
    }

    private void makeTokenRequest() {

        SuplaOAuthToken Token = getToken();
        if (Token != null && Token.isAlive()) return;

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client == null) {
            Trace.d(log_tag, "Client is not available");
            return;
        }


        client.oAuthTokenRequest();

        synchronized (this) {
            try {
                this.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected DbHelper getDbH() {
        if (DbH == null) {
            DbH = DbHelper.getInstance(_context);
        }

        return DbH;
    }

    protected MeasurementsDbHelper getMeasurementsDbH() {
        if (MDbH == null) {
            MDbH = MeasurementsDbHelper.getInstance(_context);
        }

        return MDbH;
    }

    private ApiRequestResult apiRequest(boolean retry, String endpint) {

        makeTokenRequest();
        SuplaOAuthToken Token = getToken();

        if (Token == null) {
            Trace.d(log_tag, "Token == null");
            return null;
        }

        if (Token.getUrl() == null) {
            Trace.d(log_tag, "Token.getUrl() == null");
            return null;
        }

        URL url = Token.getUrl();
        Uri.Builder builder = new Uri.Builder();

        builder.scheme(url.getProtocol())
                .encodedAuthority(url.getAuthority())
                .path(url.getPath())
                .appendPath("api")
                .appendPath("2.2.0")
                .appendEncodedPath(endpint);

        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        if (url == null) {
            return null;
        }

        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        try {
            TrustManager[] trustSelfSignedCerts = null;

            if (!url.getAuthority().contains(".supla.org")) {
                trustSelfSignedCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType)
                                    throws CertificateException {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType)
                                    throws CertificateException {
                                if (certs == null) {
                                    throw new IllegalArgumentException("Cert is null");
                                }
                                if (certs.length > 1) {
                                    TrustManagerFactory tmf = null;
                                    try {
                                        tmf = TrustManagerFactory
                                                .getInstance(TrustManagerFactory.
                                                        getDefaultAlgorithm());
                                    } catch (NoSuchAlgorithmException e) {
                                        throw new CertificateException("Can't verify certificate. "
                                                +e.getMessage());
                                    }
                                    try {
                                        tmf.init((KeyStore) null);
                                    } catch (KeyStoreException e) {
                                        throw new CertificateException("Can't verify certificate. "
                                                +e.getMessage());
                                    }
                                    TrustManager[] trustManagers = tmf.getTrustManagers();

                                    for( TrustManager tm :  trustManagers ) {
                                        if (tm instanceof X509TrustManager) {
                                            ((X509TrustManager) tm)
                                                    .checkServerTrusted(certs, authType);
                                        }
                                    }
                                }
                            }
                        }
                };

                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            sc.init(null, trustSelfSignedCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
        conn.setSSLSocketFactory(sc.getSocketFactory());

        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        }

        ApiRequestResult result = null;


        try {
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            conn.addRequestProperty("Authorization", "Bearer " + Token.getToken());

            conn.connect();
            try {
                Trace.d(log_tag, "CODE: " + conn.getResponseCode());
                Trace.d(log_tag, "URL: " + url.toString());

                int TotalCount;
                try {
                    TotalCount = Integer.parseInt(conn.getHeaderField("X-Total-Count"));
                } catch (NumberFormatException e) {
                    TotalCount = 0;
                }

                JsonReader reader = new JsonReader(
                        new InputStreamReader(conn.getResponseCode() == 200 ?
                                conn.getInputStream() : conn.getErrorStream(), "UTF-8"));

                InputStream ins = new BufferedInputStream(conn.getResponseCode() == 200 ?
                        conn.getInputStream() : conn.getErrorStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(ins));

                String inputLine;
                StringBuffer sb = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }

                //Trace.d(log_tag, sb.toString());
                //Trace.d(log_tag, "Result size: "+Integer.toString(sb.length()));
                Object obj = new JSONTokener(sb.toString()).nextValue();
                result = new ApiRequestResult(obj, conn.getResponseCode(), TotalCount);


            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null && result.getCode() == 401 && retry) {
            setToken(null);
            result = apiRequest(false, endpint);
        }

        return result;
    }

    protected ApiRequestResult apiRequest(String endpint) {

        return apiRequest(true, endpint);
    }

    public interface IAsyncResults {
        void onRestApiTaskStarted(SuplaRestApiClientTask task);

        void onRestApiTaskFinished(SuplaRestApiClientTask task);

        void onRestApiTaskProgressUpdate(SuplaRestApiClientTask task, Double progress);
    }

    protected class ApiRequestResult {

        private Object JObj;
        private int Code;
        private int TotalCount;

        ApiRequestResult(Object jobj, int code, int totalCount) {
            JObj = jobj;
            Code = code;
            TotalCount = totalCount;
        }

        public Object getJObj() {
            return JObj;
        }

        public int getCode() {
            return Code;
        }

        public int getTotalCount() {
            return TotalCount;
        }
    }

}
