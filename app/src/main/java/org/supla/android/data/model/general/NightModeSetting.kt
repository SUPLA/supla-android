package org.supla.android.data.model.general
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

enum class NightModeSetting(val value: Int) {
  ALWAYS(0),
  NEVER(1),
  AUTO(2),
  UNSET(-1);

  fun modeManagerValue() =
    when (this) {
      AUTO -> UiModeManager.MODE_NIGHT_AUTO
      NEVER, UNSET -> UiModeManager.MODE_NIGHT_NO
      ALWAYS -> UiModeManager.MODE_NIGHT_YES
    }

  fun appCompatDelegateValue() =
    when (this) {
      AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      NEVER, UNSET -> AppCompatDelegate.MODE_NIGHT_NO
      ALWAYS -> AppCompatDelegate.MODE_NIGHT_YES
    }

  companion object {
    fun from(value: Int): NightModeSetting {
      for (mode in NightModeSetting.values()) {
        if (mode.value == value) {
          return mode
        }
      }

      return UNSET
    }
  }
}
