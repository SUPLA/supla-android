package org.supla.android
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
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import androidx.core.content.edit
import timber.log.Timber

const val PREF_CHANNEL_HEIGHT: String = "pref_channel_height_percent"

private const val PREF_BUTTON_AUTOHIDE = "pref_button_autohide"
private const val PREF_SHON_CHANNEL_INFO = "pref_show_channel_info"
private const val PREF_SHOW_BOTTOM_LABEL = "pref_show_bottom_label"
private const val PREF_SHOW_BOTTOM_MENU = "pref_show_bottom_menu"
private const val PREF_SHOW_OPENING_PERCENT = "pref_show_opening_percent"

private const val PREF_NEW_GESTURE_INFO = "pref_new_gesture_info"
private const val PREF_THERMOSTAT_SCHEDULE_INFO = "pref_thermostat_schedule_info"
private const val PREF_NOTIFICATION_ASKED = "pref_notifications_asked"
private const val PREF_SHOULD_SHOW_NEW_GESTURE_INFO = "pref_should_show_new_gesture_info"
private const val PREF_SHOULD_SHOW_EM_HISTORY_INTRODUCTION = "pref_should_show_em_history_introduction"
private const val PREF_SHOULD_SHOW_EM_GENERAL_INTRODUCTION = "pref_should_show_em_general_introduction"
private const val PREF_PLAY_ANDROID_AUTO = "pref_play_android_auto"
private const val PREF_NFC_LOCK_TAG = "pref_nfc_lock_tag"

class Preferences(context: Context) {
  private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  val scale: Float
    get() = this.channelHeight / 100f

  var isButtonAutohide: Boolean
    get() = sharedPreferences.getBoolean(PREF_BUTTON_AUTOHIDE, true)
    set(value) = sharedPreferences.edit { putBoolean(PREF_BUTTON_AUTOHIDE, value) }

  var channelHeight: Int
    get() = sharedPreferences.getInt(PREF_CHANNEL_HEIGHT, 100)
    set(value) = sharedPreferences.edit { putInt(PREF_CHANNEL_HEIGHT, value) }

  var isShowChannelInfo: Boolean
    get() = sharedPreferences.getBoolean(PREF_SHON_CHANNEL_INFO, true)
    set(value) = sharedPreferences.edit { putBoolean(PREF_SHON_CHANNEL_INFO, value) }

  var isShowBottomLabel: Boolean
    get() = sharedPreferences.getBoolean(PREF_SHOW_BOTTOM_LABEL, true)
    set(value) = sharedPreferences.edit { putBoolean(PREF_SHOW_BOTTOM_LABEL, value) }

  var isShowBottomMenu: Boolean
    get() = sharedPreferences.getBoolean(PREF_SHOW_BOTTOM_MENU, true)
    set(value) = sharedPreferences.edit { putBoolean(PREF_SHOW_BOTTOM_MENU, value) }

  var isShowOpeningPercent: Boolean
    get() = sharedPreferences.getBoolean(PREF_SHOW_OPENING_PERCENT, false)
    set(value) = sharedPreferences.edit { putBoolean(PREF_SHOW_OPENING_PERCENT, value) }

  var isNewGestureInfoPresented: Boolean
    get() = sharedPreferences.getBoolean(PREF_NEW_GESTURE_INFO, false)
    set(presented) = sharedPreferences.edit { putBoolean(PREF_NEW_GESTURE_INFO, presented) }

  var showThermostatScheduleInfo: Boolean
    get() = sharedPreferences.getBoolean(PREF_THERMOSTAT_SCHEDULE_INFO, true)
    set(presented) = sharedPreferences.edit { putBoolean(PREF_THERMOSTAT_SCHEDULE_INFO, presented) }

  var isNotificationsPopupDisplayed: Boolean
    get() = sharedPreferences.getBoolean(PREF_NOTIFICATION_ASKED, false)
    set(displayed) = sharedPreferences.edit { putBoolean(PREF_NOTIFICATION_ASKED, displayed) }

  var playAndroidAuto: Boolean
    get() = sharedPreferences.getBoolean(PREF_PLAY_ANDROID_AUTO, true)
    set(value) = sharedPreferences.edit { putBoolean(PREF_PLAY_ANDROID_AUTO, value) }

  var lockNfcTag: Boolean
    get() = sharedPreferences.getBoolean(PREF_NFC_LOCK_TAG, false)
    set(value) = sharedPreferences.edit { putBoolean(PREF_NFC_LOCK_TAG, value) }

  fun shouldShowNewGestureInfo(): Boolean {
    return sharedPreferences.getBoolean(PREF_SHOULD_SHOW_NEW_GESTURE_INFO, false)
  }

  fun setShouldShowNewGestureInfo() {
    sharedPreferences.edit { putBoolean(PREF_SHOULD_SHOW_NEW_GESTURE_INFO, true) }
  }

  fun shouldShowEmHistoryIntroduction(): Boolean {
    return sharedPreferences.getBoolean(PREF_SHOULD_SHOW_EM_HISTORY_INTRODUCTION, true)
  }

  fun setEmHistoryIntroductionShown() {
    sharedPreferences.edit { putBoolean(PREF_SHOULD_SHOW_EM_HISTORY_INTRODUCTION, false) }
  }

  fun shouldShowEmGeneralIntroduction(): Boolean {
    return sharedPreferences.getBoolean(PREF_SHOULD_SHOW_EM_GENERAL_INTRODUCTION, true)
  }

  fun setEmGeneralIntroductionShown() {
    sharedPreferences.edit { putBoolean(PREF_SHOULD_SHOW_EM_GENERAL_INTRODUCTION, false) }
  }

  fun registerChangeListener(listener: OnSharedPreferenceChangeListener?) {
    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
  }

  fun unregisterChangeListener(listener: OnSharedPreferenceChangeListener?) {
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
  }

  companion object {
    fun getDeviceID(ctx: Context): String {
      var id: String? = null

      try {
        id = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID)
        } else {
          Build.SERIAL
        }

        id += "-" + Build.BOARD + "-" + Build.BRAND + "-" + Build.DEVICE + "-" + Build.HARDWARE
      } catch (e: Exception) {
        Timber.e(e, "getDeviceID error")
      }

      return id ?: "unknown"
    }
  }
}
