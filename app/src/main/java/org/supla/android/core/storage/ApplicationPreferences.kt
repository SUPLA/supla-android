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
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.data.model.general.NightModeSetting
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_BATTERY_WARNING_LEVEL = 10

private const val KEY_NIGHT_MODE = "pref_night_mode"
private const val KEY_BATTERY_WARNING_LEVEL = "pref_battery_warning_level"

@Singleton
class ApplicationPreferences @Inject constructor(@ApplicationContext context: Context) {

  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

  var nightMode: NightModeSetting
    get() = NightModeSetting.from(preferences.getInt(KEY_NIGHT_MODE, NightModeSetting.UNSET.value))
    set(setting) = preferences.edit().putInt(KEY_NIGHT_MODE, setting.value).apply()

  var batteryWarningLevel: Int
    get() = preferences.getInt(KEY_BATTERY_WARNING_LEVEL, DEFAULT_BATTERY_WARNING_LEVEL)
    set(level) = preferences.edit().putInt(KEY_BATTERY_WARNING_LEVEL, level).apply()
}
