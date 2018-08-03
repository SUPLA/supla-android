package org.supla.android.lib;

import android.util.Base64;

import org.supla.android.Trace;

import java.net.MalformedURLException;
import java.net.URL;

public class SuplaOAuthToken {

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
            ResultCode = token.ResultCode;
            Birthday = token.Birthday;
            ExpiresIn = token.ExpiresIn;
            Token = token.Token == null ? null : new String(token.Token);
        }
    }

    int getResultCode() {
        return ResultCode;
    }

    int getExpiresIn() {
        return getExpiresIn();
    }

    String getToken() {
        return Token;
    }

    URL getUrl() {
        URL result = null;

        String[] t = Token.split("\\.");
        if (t.length > 1) {
            byte [] data = Base64.decode(t[t.length-1], Base64.DEFAULT);
            if (data!=null) {
                try {
                    result = new URL(new String(data));
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        }

        return result;
    }

    public boolean isAlive() {
        return Birthday+ExpiresIn - (System.currentTimeMillis() / 1000L) >= 20;
    }

}
