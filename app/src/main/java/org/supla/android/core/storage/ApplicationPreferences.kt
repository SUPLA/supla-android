package org.supla.android.core.storage
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

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.data.model.general.NightModeSetting
import org.supla.core.shared.data.model.thermometer.TemperatureUnit
import javax.inject.Inject
import javax.inject.Singleton
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences as CoreApplicationPreferences

private const val DEFAULT_CHANNEL_HEIGHT = 100
private const val DEFAULT_BATTERY_WARNING_LEVEL = 10
private const val DEFAULT_TEMPERATURE_PRECISION = 1

private const val KEY_BUTTON_AUTOHIDE = "pref_button_autohide"
private const val KEY_SHOW_CHANNEL_INFO = "pref_show_channel_info"
private const val KEY_SHOW_BOTTOM_LABEL = "pref_show_bottom_label"
private const val KEY_SHOW_BOTTOM_MENU = "pref_show_bottom_menu"
private const val KEY_SHOW_OPENING_PERCENT = "pref_show_opening_percent"
private const val KEY_HIDE_UNAVAILABLE_CHANNELS = "pref_show_unavailable_channels"
private const val KEY_NEW_GESTURE_INFO = "pref_new_gesture_info"
private const val KEY_THERMOSTAT_SCHEDULE_INFO = "pref_thermostat_schedule_info"
private const val KEY_NOTIFICATION_ASKED = "pref_notifications_asked"
private const val KEY_SHOULD_SHOW_NEW_GESTURE_INFO = "pref_should_show_new_gesture_info"
private const val KEY_SHOULD_SHOW_EM_HISTORY_INTRODUCTION = "pref_should_show_em_history_introduction"
private const val KEY_SHOULD_SHOW_EM_GENERAL_INTRODUCTION = "pref_should_show_em_general_introduction"
private const val KEY_PLAY_ANDROID_AUTO = "pref_play_android_auto"
private const val KEY_NIGHT_MODE = "pref_night_mode"
private const val KEY_BATTERY_WARNING_LEVEL = "pref_battery_warning_level"
private const val KEY_TEMPERATURE_UNIT = "pref_temperature_unit"
private const val KEY_TEMPERATURE_PRECISION = "pref_temperature_precision"
private const val KEY_ROTATION_ENABLED = "pref_rotation_enabled"

const val PREF_CHANNEL_HEIGHT = "pref_channel_height_percent"

@Singleton
class ApplicationPreferences @Inject constructor(@ApplicationContext context: Context) : CoreApplicationPreferences {

  private val preferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(context)
  }

  val scale: Float
    get() = channelHeight / 100f

  var isButtonAutohide: Boolean
    get() = preferences.getBoolean(KEY_BUTTON_AUTOHIDE, true)
    set(value) = preferences.edit { putBoolean(KEY_BUTTON_AUTOHIDE, value) }

  var channelHeight: Int
    get() = preferences.getInt(PREF_CHANNEL_HEIGHT, DEFAULT_CHANNEL_HEIGHT)
    set(value) = preferences.edit { putInt(PREF_CHANNEL_HEIGHT, value) }

  var isShowChannelInfo: Boolean
    get() = preferences.getBoolean(KEY_SHOW_CHANNEL_INFO, true)
    set(value) = preferences.edit { putBoolean(KEY_SHOW_CHANNEL_INFO, value) }

  var isShowBottomLabel: Boolean
    get() = preferences.getBoolean(KEY_SHOW_BOTTOM_LABEL, true)
    set(value) = preferences.edit { putBoolean(KEY_SHOW_BOTTOM_LABEL, value) }

  var isShowBottomMenu: Boolean
    get() = preferences.getBoolean(KEY_SHOW_BOTTOM_MENU, true)
    set(value) = preferences.edit { putBoolean(KEY_SHOW_BOTTOM_MENU, value) }

  var isShowOpeningPercent: Boolean
    get() = preferences.getBoolean(KEY_SHOW_OPENING_PERCENT, false)
    set(value) = preferences.edit { putBoolean(KEY_SHOW_OPENING_PERCENT, value) }

  var hideUnavailableChannels: Boolean
    get() = preferences.getBoolean(KEY_HIDE_UNAVAILABLE_CHANNELS, false)
    set(value) = preferences.edit { putBoolean(KEY_HIDE_UNAVAILABLE_CHANNELS, value) }

  var isNewGestureInfoPresented: Boolean
    get() = preferences.getBoolean(KEY_NEW_GESTURE_INFO, false)
    set(presented) = preferences.edit { putBoolean(KEY_NEW_GESTURE_INFO, presented) }

  var showThermostatScheduleInfo: Boolean
    get() = preferences.getBoolean(KEY_THERMOSTAT_SCHEDULE_INFO, true)
    set(presented) = preferences.edit { putBoolean(KEY_THERMOSTAT_SCHEDULE_INFO, presented) }

  var isNotificationsPopupDisplayed: Boolean
    get() = preferences.getBoolean(KEY_NOTIFICATION_ASKED, false)
    set(displayed) = preferences.edit { putBoolean(KEY_NOTIFICATION_ASKED, displayed) }

  var playAndroidAuto: Boolean
    get() = preferences.getBoolean(KEY_PLAY_ANDROID_AUTO, true)
    set(value) = preferences.edit { putBoolean(KEY_PLAY_ANDROID_AUTO, value) }

  var nightMode: NightModeSetting
    get() = NightModeSetting.from(preferences.getInt(KEY_NIGHT_MODE, NightModeSetting.UNSET.value))
    set(setting) = preferences.edit { putInt(KEY_NIGHT_MODE, setting.value) }

  var rotationEnabled: Boolean
    get() = preferences.getBoolean(KEY_ROTATION_ENABLED, false)
    set(enabled) = preferences.edit { putBoolean(KEY_ROTATION_ENABLED, enabled) }

  override var batteryWarningLevel: Int
    get() = preferences.getInt(KEY_BATTERY_WARNING_LEVEL, DEFAULT_BATTERY_WARNING_LEVEL)
    set(level) = preferences.edit { putInt(KEY_BATTERY_WARNING_LEVEL, level) }

  override var temperaturePrecision: Int
    get() = preferences.getInt(KEY_TEMPERATURE_PRECISION, DEFAULT_TEMPERATURE_PRECISION)
    set(precision) = preferences.edit { putInt(KEY_TEMPERATURE_PRECISION, precision) }

  override var temperatureUnit: TemperatureUnit
    get() = TemperatureUnit.fromValue(preferences.getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.rawValue))
    set(unit) = preferences.edit { putString(KEY_TEMPERATURE_UNIT, unit.rawValue) }

  fun shouldShowNewGestureInfo(): Boolean {
    return preferences.getBoolean(KEY_SHOULD_SHOW_NEW_GESTURE_INFO, false)
  }

  fun setShouldShowNewGestureInfo() {
    preferences.edit { putBoolean(KEY_SHOULD_SHOW_NEW_GESTURE_INFO, true) }
  }

  fun shouldShowEmHistoryIntroduction(): Boolean {
    return preferences.getBoolean(KEY_SHOULD_SHOW_EM_HISTORY_INTRODUCTION, true)
  }

  fun setEmHistoryIntroductionShown() {
    preferences.edit { putBoolean(KEY_SHOULD_SHOW_EM_HISTORY_INTRODUCTION, false) }
  }

  fun shouldShowEmGeneralIntroduction(): Boolean {
    return preferences.getBoolean(KEY_SHOULD_SHOW_EM_GENERAL_INTRODUCTION, true)
  }

  fun setEmGeneralIntroductionShown() {
    preferences.edit { putBoolean(KEY_SHOULD_SHOW_EM_GENERAL_INTRODUCTION, false) }
  }

  fun registerChangeListener(listener: OnSharedPreferenceChangeListener?) {
    preferences.registerOnSharedPreferenceChangeListener(listener)
  }

  fun unregisterChangeListener(listener: OnSharedPreferenceChangeListener?) {
    preferences.unregisterOnSharedPreferenceChangeListener(listener)
  }
}
