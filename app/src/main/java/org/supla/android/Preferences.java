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
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit;

public class Preferences {

  private static final String TAG = Preferences.class.getSimpleName();

  private static final String pref_brightness_picker_type_slider =
      "pref_brightness_picker_type_slider";
  private static final String pref_temperature_unit = "pref_temperature_unit";
  private static final String pref_button_autohide = "pref_button_autohide";
  public static final String pref_channel_height = "pref_channel_height_percent";
  private static final String pref_show_channel_info = "pref_show_channel_info";
  private static final String pref_show_bottom_label = "pref_show_bottom_label";
  private static final String pref_show_bottom_menu = "pref_show_bottom_menu";
  private static final String pref_show_opening_percent = "pref_show_opening_percent";

  private static final String pref_new_gesture_info = "pref_new_gesture_info";
  private static final String pref_thermostat_schedule_info = "pref_thermostat_schedule_info";
  private static final String pref_notifications_asked = "pref_notifications_asked";
  private static final String pref_should_show_new_gesture_info =
      "pref_should_show_new_gesture_info";
  private static final String pref_should_show_em_history_introduction =
      "pref_should_show_em_history_introduction";
  private static final String pref_should_show_em_general_introduction =
      "pref_should_show_em_general_introduction";
  private static final String pref_play_android_auto = "pref_play_android_auto";

  private final SharedPreferences _prefs;

  public Preferences(Context context) {
    _prefs = PreferenceManager.getDefaultSharedPreferences(context);
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

    return id == null ? "unknown" : id;
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
    return v.charAt(0) == 'F' ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
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

  public float getScale() {
    return getChannelHeight() / 100f;
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

  public boolean isShowBottomLabel() {
    return _prefs.getBoolean(pref_show_bottom_label, true);
  }

  public void setShowBottomLabel(boolean val) {
    SharedPreferences.Editor ed = _prefs.edit();
    ed.putBoolean(pref_show_bottom_label, val);
    ed.apply();
  }

  public boolean isShowBottomMenu() {
    return _prefs.getBoolean(pref_show_bottom_menu, true);
  }

  public void setShowBottomMenu(boolean val) {
    SharedPreferences.Editor ed = _prefs.edit();
    ed.putBoolean(pref_show_bottom_menu, val);
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

  public boolean isNewGestureInfoPresented() {
    return _prefs.getBoolean(pref_new_gesture_info, false);
  }

  public void setNewGestureInfoPresented(boolean presented) {
    _prefs.edit().putBoolean(pref_new_gesture_info, presented).apply();
  }

  public boolean getShowThermostatScheduleInfo() {
    return _prefs.getBoolean(pref_thermostat_schedule_info, true);
  }

  public void setShowThermostatScheduleInfo(boolean presented) {
    _prefs.edit().putBoolean(pref_thermostat_schedule_info, presented).apply();
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

  public boolean shouldShowEmHistoryIntroduction() {
    return _prefs.getBoolean(pref_should_show_em_history_introduction, true);
  }

  public void setEmHistoryIntroductionShown() {
    _prefs.edit().putBoolean(pref_should_show_em_history_introduction, false).apply();
  }

  public boolean shouldShowEmGeneralIntroduction() {
    return _prefs.getBoolean(pref_should_show_em_general_introduction, true);
  }

  public void setEmGeneralIntroductionShown() {
    _prefs.edit().putBoolean(pref_should_show_em_general_introduction, false).apply();
  }

  public boolean playAndroidAuto() {
    return _prefs.getBoolean(pref_play_android_auto, true);
  }

  public void setPlayAndroidAuto(boolean active) {
    _prefs.edit().putBoolean(pref_play_android_auto, active).apply();
  }

  public void registerChangeListener(OnSharedPreferenceChangeListener listener) {
    _prefs.registerOnSharedPreferenceChangeListener(listener);
  }

  public void unregisterChangeListener(OnSharedPreferenceChangeListener listener) {
    _prefs.unregisterOnSharedPreferenceChangeListener(listener);
  }
}
