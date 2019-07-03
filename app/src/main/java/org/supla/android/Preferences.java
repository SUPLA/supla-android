package org.supla.android;

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

import android.os.Build;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
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
    private static final String pref_wizard_save_password = "pref_wizard_save_password";
    private static final String pref_wizard_password = "pref_wizard_password";
    private static final String pref_wizard_selected_wifi = "pref_wizard_selected_wifi";
    private static final String pref_hp_turbo_time = "pref_hp_turbo_time";
    private static final String pref_hp_eco_reduction = "pref_hp_eco_reduction";

    private SharedPreferences _prefs;
    private Context _context;

    public Preferences(Context context) {
        _prefs = PreferenceManager.getDefaultSharedPreferences(context);
        _context = context;

        if ( getCfgVersion() == 0 ) {

            setAdvancedCfg(!getServerAddress().isEmpty() && getAccessID() != 0 && !getAccessIDpwd().isEmpty());
            setCfgVersion(2);
        }

        context.getContentResolver();
    }

    private int getCfgVersion() {
        return _prefs.getInt(pref_cfg_ver, 0);
    }

    public void setCfgVersion(int version) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_cfg_ver, version);
        editor.apply();
    }

    private String getDeviceID() {
        String Id = null;

        try {
            final TelephonyManager tm = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Id = Settings.Secure.getString(_context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else {
                Id =  Build.SERIAL;
            }

            Id += "-" + Build.BOARD+
                    "-"+Build.BRAND+
                    "-"+Build.DEVICE+
                    "-"+Build.HARDWARE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Id == null || Id.length() == 0) ? "unknown" : Id;
    }

    private void encryptAndSave(String pref_key, byte[] data) {
            SharedPreferences.Editor editor = _prefs.edit();
            editor.putString(pref_key,
                    Base64.encodeToString(
                            Encryption.encryptDataWithNullOnException(
                                    data, getDeviceID()), Base64.DEFAULT));
            editor.putBoolean(pref_key+"_encrypted", true);
            editor.apply();
    }

    private byte[] getRandom(String pref_key, int size) {

        byte[] result = Base64.decode(_prefs.getString(pref_key, ""), Base64.DEFAULT);

        if ( !_prefs.getBoolean(pref_key+"_encrypted", false) ) {
            encryptAndSave(pref_key, result);
        } else {
            result = Encryption.decryptDataWithNullOnException(result, getDeviceID());
        }

        if ( result == null || result.length != size ) {

            Random random = new Random();
            result = new byte[size];

            for(int a=0;a<size;a++) {
                result[a] = (byte)random.nextInt(255);
            }

            encryptAndSave(pref_key, result);
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
        editor.apply();
    }

    public int getAccessID() {
        return _prefs.getInt(pref_accessid, 0);
    }

    public void setAccessID(int AccessID) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_accessid, AccessID);
        editor.apply();
    }

    public String getAccessIDpwd() {
        return _prefs.getString(pref_accessidpwd, "");
    }

    public void setAccessIDpwd(String AccessIDpwd) {

        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_accessidpwd, AccessIDpwd);
        editor.apply();
    }

    public String getEmail() {
        return _prefs.getString(pref_email, "");
    }

    public void setEmail(String email) {

        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_email, email);
        editor.apply();
    }

    public boolean configIsSet() {

        if ( isAdvancedCfg() )
            return !getServerAddress().equals("") && getAccessID() != 0 && !getAccessIDpwd().equals("");

        return !getEmail().equals("");
    }

    public void setAdvancedCfg(Boolean advanced) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putBoolean(pref_advanced, advanced);
        editor.apply();
    }

    public boolean isAdvancedCfg() {
        return _prefs.getBoolean(pref_advanced, false);
    }

    public int getPreferedProtocolVersion() {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        return _prefs.getInt(pref_proto_ver, client == null ? 0 : client.GetMaxProtoVersion());
    }

    public void setPreferedProtocolVersion(int version) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_proto_ver, version);
        editor.apply();
    }

    public void setPreferedProtocolVersion() {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        setPreferedProtocolVersion(client == null ? 0 : client.GetMaxProtoVersion());
    }

    public boolean wizardSavePasswordEnabled(String SSID) {
        return _prefs.getBoolean(pref_wizard_save_password+SSID, true);
    }

    public void wizardSetSavePasswordEnabled(String SSID, boolean enabled) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putBoolean(pref_wizard_save_password+SSID, enabled);
        editor.apply();
    }

    public String wizardGetPassword(String SSID) {
        return _prefs.getString(pref_wizard_password+SSID, "");
    }

    public void wizardSetPassword(String SSID, String password) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_wizard_password+SSID, password);
        editor.apply();
    }

    public String wizardGetSelectedWifi() {
        return _prefs.getString(pref_wizard_selected_wifi, "");
    }

    public void wizardSetSelectedWifi(String SSID) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_wizard_selected_wifi, SSID);
        editor.apply();
    }

    public void setHeatpolTurboTime(int time) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_hp_turbo_time, time);
        editor.apply();
    }

    public int getHeatpolTurboTime() {
        return _prefs.getInt(pref_hp_turbo_time, 1);
    }

    public void setHeatpolEcoReduction(int temp) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putInt(pref_hp_eco_reduction, temp);
        editor.apply();
    }

    public int getHeatpolEcoReduction() {
        return _prefs.getInt(pref_hp_eco_reduction, 5);
    }

}
