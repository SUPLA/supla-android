package org.supla.android.features.details.thermostatdetail.timer
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

import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.ui.views.SegmentedComponentItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

enum class DeviceMode(@param:StringRes val stringRes: Int) : SegmentedComponentItem {
  OFF(R.string.turn_off),
  MANUAL(R.string.details_timer_manual_mode),

  AUTO(R.string.auto),
  HEATING(R.string.hvac_mode_heating),
  COOLING(R.string.hvac_mode_cooling);

  override val label: LocalizedString = localizedString(stringRes)

  companion object {
    val defaultModes = listOf(OFF, MANUAL)
    val heatCoolModes = listOf(OFF, AUTO, HEATING, COOLING)
  }
}
