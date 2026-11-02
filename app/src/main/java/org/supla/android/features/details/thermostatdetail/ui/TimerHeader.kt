package org.supla.android.features.details.thermostatdetail.ui
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

import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import java.util.Date

object TimerHeaderHelper {
  fun endDateText(timerEndDate: Date?): LocalizedString {
    val (endDate) = guardLet(timerEndDate) { return LocalizedString.Empty }
    return localizedString(R.string.details_timer_state_label_for_timer_days, ValuesFormatter.getFullDateString(endDate) ?: "")
  }

  fun currentStateIcon(mode: SuplaHvacMode?): Int? =
    when (mode) {
      SuplaHvacMode.HEAT -> R.drawable.ic_heat
      SuplaHvacMode.COOL -> R.drawable.ic_cool
      else -> null
    }

  fun currentStateIconColor(mode: SuplaHvacMode?): Int =
    when (mode) {
      SuplaHvacMode.HEAT -> R.color.red
      SuplaHvacMode.COOL -> R.color.blue
      else -> R.color.disabled
    }

  fun currentStateValue(
    mode: SuplaHvacMode?,
    heatSetpoint: Float?,
    coolSetpoint: Float?,
    thermometerValuesFormatter: ValueFormatter
  ): LocalizedString =
    when (mode) {
      SuplaHvacMode.OFF -> LocalizedString.Constant("OFF")
      SuplaHvacMode.HEAT -> LocalizedString.Constant(thermometerValuesFormatter.format(heatSetpoint, ValueFormat.WithoutUnit))
      SuplaHvacMode.COOL -> LocalizedString.Constant(thermometerValuesFormatter.format(coolSetpoint, ValueFormat.WithoutUnit))
      SuplaHvacMode.HEAT_COOL -> {
        val min = thermometerValuesFormatter.format(heatSetpoint, ValueFormat.WithoutUnit)
        val max = thermometerValuesFormatter.format(coolSetpoint, ValueFormat.WithoutUnit)
        LocalizedString.Constant("$min - $max")
      }
      else -> LocalizedString.Empty
    }
}
