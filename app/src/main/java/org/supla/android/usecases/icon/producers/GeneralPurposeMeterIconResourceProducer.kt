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
      1 -> data.icon(R.drawable.fnc_heatmeter, R.drawable.fnc_heatmeter_nm)
      2 -> data.icon(R.drawable.fnc_gasmeter, R.drawable.fnc_gasmeter_nm)
      3 -> data.icon(R.drawable.fnc_watermeter, R.drawable.fnc_watermeter_nm)
      4 -> data.icon(R.drawable.fnc_gasmeter, R.drawable.fnc_gasmeter_nm)
      5 -> data.icon(R.drawable.fnc_gpm_septic_tank_1, R.drawable.fnc_gpm_septic_tank_1_nm)
      6 -> data.icon(R.drawable.fnc_gpm_septic_tank_2, R.drawable.fnc_gpm_septic_tank_2_nm)
      7 -> data.icon(R.drawable.fnc_gpm_septic_tank_3, R.drawable.fnc_gpm_septic_tank_3_nm)
      8 -> data.icon(R.drawable.fnc_gpm_septic_tank_4, R.drawable.fnc_gpm_septic_tank_4_nm)
      9 -> data.icon(R.drawable.fnc_gpm_water_tank_1, R.drawable.fnc_gpm_water_tank_1_nm)
      10 -> data.icon(R.drawable.fnc_gpm_water_tank_2, R.drawable.fnc_gpm_water_tank_2_nm)
      11 -> data.icon(R.drawable.fnc_gpm_water_tank_3, R.drawable.fnc_gpm_water_tank_3_nm)
      12 -> data.icon(R.drawable.fnc_gpm_coal, R.drawable.fnc_gpm_coal_nm)
      13 -> data.icon(R.drawable.fnc_gpm_salt, R.drawable.fnc_gpm_salt_nm)
      else -> data.icon(R.drawable.fnc_gpm_5, R.drawable.fnc_gpm_5_nm)
    }
}
