package org.supla.android.lib;

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
