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
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer

class GeneralPurposeMeterIconResourceProducer : IconResourceProducer {
  override fun accepts(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> R.drawable.fnc_electricity_meter
      2 -> R.drawable.fnc_heatmeter
      3 -> R.drawable.fnc_watermeter
      4 -> R.drawable.fnc_gasmeter
      5 -> R.drawable.fnc_gpm_septic_tank_1
      6 -> R.drawable.fnc_gpm_septic_tank_2
      7 -> R.drawable.fnc_gpm_septic_tank_3
      8 -> R.drawable.fnc_gpm_septic_tank_4
      9 -> R.drawable.fnc_gpm_water_tank_1
      10 -> R.drawable.fnc_gpm_water_tank_2
      11 -> R.drawable.fnc_gpm_water_tank_3
      12 -> R.drawable.fnc_gpm_coal
      13 -> R.drawable.fnc_gpm_salt
      14 -> R.drawable.fnc_gpm_klop
      else -> R.drawable.fnc_gpm_5
    }
}
