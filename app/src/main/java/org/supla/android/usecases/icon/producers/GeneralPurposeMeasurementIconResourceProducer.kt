package org.supla.android.usecases.icon.producers
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
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction

class GeneralPurposeMeasurementIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> R.drawable.fnc_gpm_1
      2 -> R.drawable.fnc_gpm_2
      3 -> R.drawable.fnc_gpm_3
      4 -> R.drawable.fnc_gpm_4
      5 -> R.drawable.fnc_gpm_air_1
      6 -> R.drawable.fnc_gpm_air_2
      7 -> R.drawable.fnc_gpm_air_3
      8 -> R.drawable.fnc_gpm_chimnay
      9 -> R.drawable.fnc_gpm_current_1
      10 -> R.drawable.fnc_gpm_current_2
      11 -> R.drawable.fnc_gpm_fan_1
      12 -> R.drawable.fnc_gpm_fan_2
      13 -> R.drawable.fnc_gpm_insolation_1
      14 -> R.drawable.fnc_gpm_insolation_2
      15 -> R.drawable.fnc_gpm_multimeter
      16 -> R.drawable.fnc_gpm_pm_1
      17 -> R.drawable.fnc_gpm_pm_2_5
      18 -> R.drawable.fnc_gpm_pm_10
      19 -> R.drawable.fnc_gpm_processor
      20 -> R.drawable.fnc_gpm_smog_1
      21 -> R.drawable.fnc_gpm_smog_2
      22 -> R.drawable.fnc_gpm_smog_3
      23 -> R.drawable.fnc_gpm_smog_4
      24 -> R.drawable.fnc_gpm_smog_5
      25 -> R.drawable.fnc_gpm_smog_6
      26 -> R.drawable.fnc_gpm_sound_1
      27 -> R.drawable.fnc_gpm_sound_2
      28 -> R.drawable.fnc_gpm_sound_3
      29 -> R.drawable.fnc_gpm_transfer
      30 -> R.drawable.fnc_gpm_voltage_1
      31 -> R.drawable.fnc_gpm_voltage_2
      else -> R.drawable.fnc_gpm_5
    }
}
