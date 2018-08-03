package org.supla.android.lib;

import android.content.Context;
import android.util.Base64;

import org.supla.android.Trace;

import java.net.MalformedURLException;
import java.net.URL;

public class SuplaOAuthToken {

    private URL Url;
    private long Birthday;
    private int ResultCode;
    private int ExpiresIn;
    private String Token;

    public SuplaOAuthToken(int resultCode, int expiresIn, String token) {
        ResultCode = resultCode;
        Birthday = System.currentTimeMillis() / 1000L;
        ExpiresIn = expiresIn;
        Token = token;
    }

    public SuplaOAuthToken(SuplaOAuthToken token) {
        if (token != null) {
            Url = token.Url;
            ResultCode = token.ResultCode;
            Birthday = token.Birthday;
            ExpiresIn = token.ExpiresIn;
            Token = token.Token == null ? null : new String(token.Token);
        }
    }

    public int getResultCode() {
        return ResultCode;
    }

    public int getExpiresIn() {
        return getExpiresIn();
    }

    public String getToken() {
        return Token;
    }

    public URL getUrl() {

        if (Url != null) {
            return Url;
        }

        String[] t = Token.split("\\.");
        if (t.length > 1) {
            byte [] data = Base64.decode(t[t.length-1], Base64.DEFAULT);
            if (data!=null) {
                try {
                    Url = new URL(new String(data));
                } catch (MalformedURLException e) {
                    Url = null;
                }
            }
        }


        return Url;
    }

    public void setUrl(URL url) {
        Url = url;
    }

    public boolean isAlive() {
        return Birthday+ExpiresIn - (System.currentTimeMillis() / 1000L) >= 20;
    }

}
