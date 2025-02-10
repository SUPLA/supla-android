package org.supla.android.features.details.electricitymeterdetail.history
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
import org.supla.android.ui.views.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString

private const val KWH = "[kWh]"

enum class ElectricityMeterChartType(@StringRes override val labelRes: Int) : SpinnerItem {
  FORWARDED_ACTIVE_ENERGY(R.string.details_em_forward_active_energy),
  REVERSED_ACTIVE_ENERGY(R.string.details_em_reverse_active_energy),
  FORWARDED_REACTIVE_ENERGY(R.string.details_em_forward_reactive_energy),
  REVERSED_REACTIVE_ENERGY(R.string.details_em_reverse_reactive_energy),
  BALANCE_ARITHMETIC(R.string.details_em_balance_arithmetic),
  BALANCE_VECTOR(R.string.details_em_balance_vector),
  BALANCE_HOURLY(R.string.details_em_balance_hourly),
  BALANCE_CHART_AGGREGATED(R.string.details_em_balance_chart_aggregated),
  VOLTAGE(R.string.details_em_voltage),
  CURRENT(R.string.details_em_current),
  POWER_ACTIVE(R.string.details_em_power_active);

  val label: LocalizedString
    get() = when (this) {
      VOLTAGE -> LocalizedString.WithResourceAndValue(labelRes, "[V]")
      CURRENT -> LocalizedString.WithResourceAndValue(labelRes, "[A]")
      POWER_ACTIVE -> LocalizedString.WithResourceAndValue(labelRes, "[W]")
      else -> LocalizedString.WithResourceAndValue(labelRes, KWH)
    }

  val needsPhases: Boolean
    get() = when (this) {
      FORWARDED_ACTIVE_ENERGY,
      REVERSED_ACTIVE_ENERGY,
      FORWARDED_REACTIVE_ENERGY,
      REVERSED_REACTIVE_ENERGY,
      VOLTAGE,
      CURRENT,
      POWER_ACTIVE -> true

      BALANCE_VECTOR,
      BALANCE_ARITHMETIC,
      BALANCE_HOURLY,
      BALANCE_CHART_AGGREGATED -> false
    }

  val hideRankings: Boolean
    get() = when (this) {
      BALANCE_VECTOR,
      BALANCE_ARITHMETIC,
      BALANCE_HOURLY,
      BALANCE_CHART_AGGREGATED,
      VOLTAGE,
      CURRENT,
      POWER_ACTIVE -> true

      FORWARDED_ACTIVE_ENERGY,
      REVERSED_ACTIVE_ENERGY,
      FORWARDED_REACTIVE_ENERGY,
      REVERSED_REACTIVE_ENERGY -> false
    }

  fun needsRefresh(otherType: ElectricityMeterChartType): Boolean {
    if (this == otherType) {
      return false
    }
    if (this == VOLTAGE || otherType == VOLTAGE) {
      return true
    }
    if (this == CURRENT || otherType == CURRENT) {
      return true
    }
    if (this == POWER_ACTIVE || otherType == POWER_ACTIVE) {
      return true
    }

    return false
  }
}
