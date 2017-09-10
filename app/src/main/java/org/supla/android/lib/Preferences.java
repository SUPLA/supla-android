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
    private static final String pref_email = "pref_email";
    private static final String pref_authkey = "pref_authkey";
    private static final String pref_advanced = "pref_advanced";
    private static final String pref_cfg_ver = "pref_cfg_ver";
    private static final String pref_proto_ver = "pref_proto_ver";

    private SharedPreferences _prefs;

    public Preferences(Context context) {
        _prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if ( getCfgVersion() == 0 ) {

            setAdvancedCfg(!getServerAddress().isEmpty() && getAccessID() != 0 && !getAccessIDpwd().isEmpty());
            setCfgVersion(2);
        }
    }

    private int getCfgVersion() {
        return _prefs.getInt(pref_cfg_ver, 0);
    }

    public void setCfgVersion(int version) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_cfg_ver, version);
        editor.commit();
    }

    private byte[] getRandom(String pref_key, int size) {

        byte[] result = Base64.decode(_prefs.getString(pref_key, ""), Base64.DEFAULT);

        if ( result.length != size ) {

            Random random = new Random();
            result = new byte[size];

            for(int a=0;a<size;a++) {
                result[a] = (byte)random.nextInt(255);
            }

            SharedPreferences.Editor editor = _prefs.edit();
            editor.putString(pref_key, Base64.encodeToString(result, Base64.DEFAULT));
            editor.commit();

        }

        return result;

    }

    public byte[] getClientGUID() {
        return getRandom(pref_guid, SuplaConst.SUPLA_GUID_SIZE);
    }

    public byte[] getAuthKey() {
        return getRandom(pref_authkey, SuplaConst.SUPLA_AUTHKEY_SIZE);
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

    public String getEmail() {
        return _prefs.getString(pref_email, "");
    }

    public void setEmail(String email) {

        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_email, email);
        editor.commit();
    }

    public boolean configIsSet() {

        if ( isAdvancedCfg() )
            return getServerAddress().equals("") == false && getAccessID() != 0 && getAccessIDpwd().equals("") == false;

        return getEmail().equals("") == false;
    }

    public void setAdvancedCfg(Boolean advanced) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putBoolean(pref_advanced, advanced);
        editor.commit();
    }

    public boolean isAdvancedCfg() {
        return _prefs.getBoolean(pref_advanced, false);
    }

    public int getPreferedProtocolVersion() {
        return _prefs.getInt(pref_proto_ver, SuplaConst.PROTOCOL_HIGHEST_VERSION);
    }

    public void setPreferedProtocolVersion(int version) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_proto_ver, version);
        editor.commit();
    }
}
