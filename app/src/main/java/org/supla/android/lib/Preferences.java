package org.supla.android.lib;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.util.Random;

public class Preferences {

    private static final String pref_guid = "pref_guid";
    private static final String pref_serveraddr = "pref_serveraddr";
    private static final String pref_accessid = "pref_accessid";
    private static final String pref_accessidpwd = "pref_accessidpwd";

    private SharedPreferences _prefs;

    public Preferences(Context context) {
        _prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public byte[] getClientGUID() {

        byte[] result = Base64.decode(_prefs.getString(pref_guid, ""), Base64.DEFAULT);

        if ( result.length != SuplaConst.SUPLA_GUID_SIZE ) {

            Random random = new Random();
            result = new byte[SuplaConst.SUPLA_GUID_SIZE];

            for(int a=0;a<SuplaConst.SUPLA_GUID_SIZE;a++) {
                result[a] = (byte)random.nextInt(255);
            }

            SharedPreferences.Editor editor = _prefs.edit();
            editor.putString(pref_guid, Base64.encodeToString(result, Base64.DEFAULT));
            editor.commit();

        }

        return result;

    }

    public String getServerAddress() {
        return _prefs.getString(pref_serveraddr, "");
    }

    public void setServerAddress(String ServerAddress) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_serveraddr, ServerAddress);
        editor.commit();
    }

    public int getAccessID() {
        return _prefs.getInt(pref_accessid, 0);
    }

    public void setAccessID(int AccessID) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_accessid, AccessID);
        editor.commit();
    }

    public String getAccessIDpwd() {
        return _prefs.getString(pref_accessidpwd, "");
    }

    public void setAccessIDpwd(String AccessIDpwd) {

        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_accessidpwd, AccessIDpwd);
        editor.commit();
    }
}
