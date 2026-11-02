@file:JvmName("NightModeSettingAndroid")

package org.supla.android.model.general
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

import android.app.UiModeManager
import androidx.appcompat.app.AppCompatDelegate
import org.supla.core.shared.data.model.export.NightModeSetting

fun NightModeSetting.appCompatDelegateValue(): Int =
  when (this) {
    NightModeSetting.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    NightModeSetting.NEVER, NightModeSetting.UNSET -> AppCompatDelegate.MODE_NIGHT_NO
    NightModeSetting.ALWAYS -> AppCompatDelegate.MODE_NIGHT_YES
  }

fun NightModeSetting.modeManagerValue(): Int =
  when (this) {
    NightModeSetting.AUTO -> UiModeManager.MODE_NIGHT_AUTO
    NightModeSetting.NEVER, NightModeSetting.UNSET -> UiModeManager.MODE_NIGHT_NO
    NightModeSetting.ALWAYS -> UiModeManager.MODE_NIGHT_YES
  }
