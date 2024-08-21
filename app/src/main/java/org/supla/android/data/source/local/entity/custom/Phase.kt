package org.supla.android.data.source.local.entity.custom

import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.data.source.remote.channel.SuplaChannelFlag

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

enum class Phase(val value: Int, @StringRes val label: Int) {
  PHASE_1(1, R.string.details_em_phase1), PHASE_2(2, R.string.details_em_phase2), PHASE_3(3, R.string.details_em_phase3);

  val disabledFlag: SuplaChannelFlag
    get() = when (this) {
      PHASE_1 -> SuplaChannelFlag.PHASE1_UNSUPPORTED
      PHASE_2 -> SuplaChannelFlag.PHASE2_UNSUPPORTED
      PHASE_3 -> SuplaChannelFlag.PHASE3_UNSUPPORTED
    }

  companion object {
    fun from(value: Int?): Phase? {
      entries.forEach {
        if (it.value == value) {
          return it
        }
      }

      return null
    }
  }
}
