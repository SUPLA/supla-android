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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;

import org.supla.android.cfg.TemperatureUnit;
import org.supla.android.lib.SuplaConst;
import org.supla.android.profile.ProfileManager;

import java.util.Random;

public class Preferences {

    private static final String pref_guid = "pref_guid";
    private static final String pref_authkey = "pref_authkey";
    private static final String pref_wizard_save_password = "pref_wizard_save_password";
    private static final String pref_wizard_password = "pref_wizard_password";
    private static final String pref_wizard_selected_wifi = "pref_wizard_selected_wifi";
    private static final String pref_hp_turbo_time = "pref_hp_turbo_time";
    private static final String pref_hp_eco_reduction = "pref_hp_eco_reduction";
    private static final String pref_brightness_picker_type_slider
            = "pref_brightness_picker_type_slider";
    private static final String pref_temperature_unit = "pref_temperature_unit";
    private static final String pref_button_autohide = "pref_button_autohide";
    public static final String pref_channel_height = "pref_channel_height_percent";
    private static final String pref_show_channel_info = "pref_show_channel_info";
    private static final String pref_show_opening_percent = "pref_show_opening_percent";

    private static final String pref_chart_type = "pref_ct%d_prof%d_%d";

    private SharedPreferences _prefs;
    private Context _context;

    public Preferences(Context context) {
        _prefs = PreferenceManager.getDefaultSharedPreferences(context);
        _context = context;

        context.getContentResolver();
    }

    public static String getDeviceID(Context ctx) {
        String Id = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Id = Settings.Secure.getString(ctx.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else {
                Id = Build.SERIAL;
            }

            Id += "-" + Build.BOARD +
                    "-" + Build.BRAND +
                    "-" + Build.DEVICE +
                    "-" + Build.HARDWARE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (Id == null || Id.length() == 0) ? "unknown" : Id;

    }


    private String getDeviceID() {
        return Preferences.getDeviceID(_context);
    }

    private void encryptAndSave(String pref_key, byte[] data) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_key,
                Base64.encodeToString(
                        Encryption.encryptDataWithNullOnException(
                                data, getDeviceID()), Base64.DEFAULT));
        editor.putBoolean(pref_key + "_encrypted_gcm", true);
        editor.apply();
    }
    
    private byte[] getRandom(String pref_key, int size) {
        byte[] result = Base64.decode(_prefs.getString(pref_key, ""), Base64.DEFAULT);

        if (!_prefs.getBoolean(pref_key + "_encrypted_gcm", false)) {
            if (_prefs.getBoolean(pref_key + "_encrypted", false)) {
                result = Encryption.decryptDataWithNullOnException(result, getDeviceID(), true);
            }
            
            if(result != null) {
                encryptAndSave(pref_key, result);
            }
            
        } else {
            result = Encryption.decryptDataWithNullOnException(result, getDeviceID());
        }

        if (result == null || result.length != size) {

            Random random = new Random();
            result = new byte[size];

            for (int a = 0; a < size; a++) {
                result[a] = (byte) random.nextInt(255);
            }

            encryptAndSave(pref_key, result);
        }

        return result;

    }


    /**
       Legacy method. Should not be used in new code.
    */
    @Deprecated
    public byte[] getClientGUID() {
        return getRandom(pref_guid, SuplaConst.SUPLA_GUID_SIZE);
    }

    /**
       Legacy method. Should not be used in new code.
    */
    @Deprecated
    public byte[] getAuthKey() {
        return getRandom(pref_authkey, SuplaConst.SUPLA_AUTHKEY_SIZE);
    }

    public boolean configIsSet() {
        return getProfileManager().getCurrentProfile()
            .getAuthInfo().isAuthDataComplete();
    }

    public boolean wizardSavePasswordEnabled(String SSID) {
        return _prefs.getBoolean(pref_wizard_save_password + SSID, true);
    }

    public void wizardSetSavePasswordEnabled(String SSID, boolean enabled) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putBoolean(pref_wizard_save_password + SSID, enabled);
        editor.apply();
    }

    public String wizardGetPassword(String SSID) {
        return _prefs.getString(pref_wizard_password + SSID, "");
    }

    public void wizardSetPassword(String SSID, String password) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(pref_wizard_password + SSID, password);
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

    public void setBrightnessPickerTypeToSlider(boolean slider) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putBoolean(pref_brightness_picker_type_slider, slider);
        editor.apply();
    }

    public Boolean isBrightnessPickerTypeSlider() {
        if (_prefs.contains(pref_brightness_picker_type_slider)) {
            return _prefs.getBoolean(pref_brightness_picker_type_slider, false);
        }
        return null;
    }

    public TemperatureUnit getTemperatureUnit() {
        String v = _prefs.getString(pref_temperature_unit, "C");
        if (v.length() < 1) v = "C";
        switch (v.charAt(0)) {
            case 'F':
                return TemperatureUnit.FAHRENHEIT;
            default:
                return TemperatureUnit.CELSIUS;
        }
    }
    public void setTemperatureUnit(TemperatureUnit u) {
        SharedPreferences.Editor ed = _prefs.edit();
        switch(u) {
            case FAHRENHEIT: ed.putString(pref_temperature_unit, "F"); break;
            case CELSIUS: ed.putString(pref_temperature_unit, "C"); break;
        }
        ed.apply();
    }

    public boolean isButtonAutohide() {
        return _prefs.getBoolean(pref_button_autohide, true);
    }

    public void setButtonAutohide(boolean val) {
        SharedPreferences.Editor ed = _prefs.edit();
        ed.putBoolean(pref_button_autohide, val);
        ed.apply();
    }

    public int getChannelHeight() {
        return _prefs.getInt(pref_channel_height, 100);
    }

    public void setChannelHeight(int val) {
        SharedPreferences.Editor ed = _prefs.edit();
        ed.putInt(pref_channel_height, val);
        ed.apply();
    }

    public boolean isShowChannelInfo() {
        return _prefs.getBoolean(pref_show_channel_info, true);
    }

    public void setShowChannelInfo(boolean val) {
        SharedPreferences.Editor ed = _prefs.edit();
        ed.putBoolean(pref_show_channel_info, val);
        ed.apply();
    }

    public boolean isShowOpeningPercent() {
        return _prefs.getBoolean(pref_show_opening_percent, false);
    }

    public void setShowOpeningPercent(boolean val) {
        SharedPreferences.Editor ed = _prefs.edit();
        ed.putBoolean(pref_show_opening_percent, val);
        ed.apply();
    }

    private ProfileManager getProfileManager() {
        return SuplaApp.getApp().getProfileManager();
    }

    private String getChartTypeKey(int channel, int idx) {
        int pid = (int)getProfileManager().getCurrentProfile().getId();
        return String.format(pref_chart_type, channel, pid, idx);
    }

    public int getChartType(int channel, int idx, int def) {
        return _prefs.getInt(getChartTypeKey(channel, idx), def);
    }

    public void setChartType(int channel, int idx,  int charttype) {
        SharedPreferences.Editor ed = _prefs.edit();
        ed.putInt(getChartTypeKey(channel, idx), charttype);
        ed.apply();
    }
}
