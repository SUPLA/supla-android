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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit;

public class Preferences {

  private static final String TAG = Preferences.class.getSimpleName();

  private static final String pref_guid = "pref_guid";
  private static final String pref_authkey = "pref_authkey";
  private static final String pref_wizard_save_password = "pref_wizard_save_password";
  private static final String pref_wizard_password = "pref_wizard_password";
  private static final String pref_wizard_selected_wifi = "pref_wizard_selected_wifi";
  private static final String pref_brightness_picker_type_slider =
      "pref_brightness_picker_type_slider";
  private static final String pref_temperature_unit = "pref_temperature_unit";
  private static final String pref_button_autohide = "pref_button_autohide";
  public static final String pref_channel_height = "pref_channel_height_percent";
  private static final String pref_show_channel_info = "pref_show_channel_info";
  private static final String pref_show_opening_percent = "pref_show_opening_percent";

  private static final String pref_chart_type = "pref_ct%d_prof%d_%d";

  private static final String pref_any_account_registered = "pref_any_account_registered";
  private static final String pref_new_gesture_info = "pref_new_gesture_info";
  private static final String pref_notifications_asked = "pref_notifications_asked";
  private static final String pref_should_show_new_gesture_info =
      "pref_should_show_new_gesture_info";

  private final Context _context;
  private final SharedPreferences _prefs;

  public Preferences(Context context) {
    _prefs = PreferenceManager.getDefaultSharedPreferences(context);
    _context = context;
    context.getContentResolver();
  }

  public static String getDeviceID(Context ctx) {
    String id = null;

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
      } else {
        id = Build.SERIAL;
      }

      id += "-" + Build.BOARD + "-" + Build.BRAND + "-" + Build.DEVICE + "-" + Build.HARDWARE;
    } catch (Exception e) {
      Trace.e(TAG, "getDeviceID error", e);
    }

    return (id == null || id.length() == 0) ? "unknown" : id;
  }

  private String getDeviceID() {
    return Preferences.getDeviceID(_context);
  }

  private byte[] getRandom(String pref_key) {
    byte[] result = Base64.decode(_prefs.getString(pref_key, ""), Base64.DEFAULT);

    if (!_prefs.getBoolean(pref_key + "_encrypted_gcm", false)) {
      if (_prefs.getBoolean(pref_key + "_encrypted", false)) {
        result = Encryption.decryptDataWithNullOnException(result, getDeviceID(), true);
      }
    } else {
      result = Encryption.decryptDataWithNullOnException(result, getDeviceID());
    }

    return result;
  }

  /** Legacy method. Should not be used in new code. */
  @Deprecated
  public byte[] getClientGUID() {
    return getRandom(pref_guid);
  }

  /** Legacy method. Should not be used in new code. */
  @Deprecated
  public byte[] getAuthKey() {
    return getRandom(pref_authkey);
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
    switch (u) {
      case FAHRENHEIT:
        ed.putString(pref_temperature_unit, "F");
        break;
      case CELSIUS:
        ed.putString(pref_temperature_unit, "C");
        break;
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

  private String getChartTypeKey(int profileId, int channel, int idx) {
    return String.format(pref_chart_type, channel, profileId, idx);
  }

  public int getChartType(int profileId, int channel, int idx, int def) {
    return _prefs.getInt(getChartTypeKey(profileId, channel, idx), def);
  }

  public void setChartType(int profileId, int channel, int idx, int charttype) {
    SharedPreferences.Editor ed = _prefs.edit();
    ed.putInt(getChartTypeKey(profileId, channel, idx), charttype);
    ed.apply();
  }

  public boolean isAnyAccountRegistered() {
    return _prefs.getBoolean(pref_any_account_registered, false);
  }

  public void setAnyAccountRegistered(boolean isRegistered) {
    _prefs.edit().putBoolean(pref_any_account_registered, isRegistered).apply();
  }

  public boolean isNewGestureInfoPresented() {
    return _prefs.getBoolean(pref_new_gesture_info, false);
  }

  public void setNewGestureInfoPresented(boolean presented) {
    _prefs.edit().putBoolean(pref_new_gesture_info, presented).apply();
  }

  public boolean isNotificationsPopupDisplayed() {
    return _prefs.getBoolean(pref_notifications_asked, false);
  }

  public void setNotificationsPopupDisplayed(boolean displayed) {
    _prefs.edit().putBoolean(pref_notifications_asked, displayed).apply();
  }

  public boolean shouldShowNewGestureInfo() {
    return _prefs.getBoolean(pref_should_show_new_gesture_info, false);
  }

  public void setShouldShowNewGestureInfo() {
    _prefs.edit().putBoolean(pref_should_show_new_gesture_info, true).apply();
  }

  public void registerChangeListener(OnSharedPreferenceChangeListener listener) {
    _prefs.registerOnSharedPreferenceChangeListener(listener);
  }

  public void unregisterChangeListener(OnSharedPreferenceChangeListener listener) {
    _prefs.unregisterOnSharedPreferenceChangeListener(listener);
  }
}
