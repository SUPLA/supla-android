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

class GenericPurposeMeasurementIconResourceProducer : IconResourceProducer {
  override fun accepts(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> data.icon(R.drawable.fnc_gpm_1, R.drawable.fnc_gpm_1_nm)
      2 -> data.icon(R.drawable.fnc_gpm_2, R.drawable.fnc_gpm_2_nm)
      3 -> data.icon(R.drawable.fnc_gpm_3, R.drawable.fnc_gpm_3_nm)
      4 -> data.icon(R.drawable.fnc_gpm_4, R.drawable.fnc_gpm_4_nm)
      5 -> data.icon(R.drawable.fnc_gpm_air_1, R.drawable.fnc_gpm_air_1_nm)
      6 -> data.icon(R.drawable.fnc_gpm_air_2, R.drawable.fnc_gpm_air_2_nm)
      7 -> data.icon(R.drawable.fnc_gpm_air_3, R.drawable.fnc_gpm_air_3_nm)
      8 -> data.icon(R.drawable.fnc_gpm_chimnay, R.drawable.fnc_gpm_chimnay_nm)
      9 -> data.icon(R.drawable.fnc_gpm_current_1, R.drawable.fnc_gpm_current_1_nm)
      10 -> data.icon(R.drawable.fnc_gpm_current_2, R.drawable.fnc_gpm_current_2_nm)
      11 -> data.icon(R.drawable.fnc_gpm_fan_1, R.drawable.fnc_gpm_fan_1_nm)
      12 -> data.icon(R.drawable.fnc_gpm_fan_2, R.drawable.fnc_gpm_fan_2_nm)
      13 -> data.icon(R.drawable.fnc_gpm_insolation_1, R.drawable.fnc_gpm_insolation_1_nm)
      14 -> data.icon(R.drawable.fnc_gpm_insolation_2, R.drawable.fnc_gpm_insolation_2_nm)
      15 -> data.icon(R.drawable.fnc_gpm_multimeter, R.drawable.fnc_gpm_multimeter_nm)
      16 -> data.icon(R.drawable.fnc_gpm_pm_1, R.drawable.fnc_gpm_pm_1_nm)
      17 -> data.icon(R.drawable.fnc_gpm_pm_2_5, R.drawable.fnc_gpm_pm_2_5_nm)
      18 -> data.icon(R.drawable.fnc_gpm_pm_10, R.drawable.fnc_gpm_pm_10_nm)
      19 -> data.icon(R.drawable.fnc_gpm_processor, R.drawable.fnc_gpm_processor_nm)
      20 -> data.icon(R.drawable.fnc_gpm_smog_1, R.drawable.fnc_gpm_smog_1_nm)
      21 -> data.icon(R.drawable.fnc_gpm_smog_2, R.drawable.fnc_gpm_smog_2_nm)
      22 -> data.icon(R.drawable.fnc_gpm_smog_3, R.drawable.fnc_gpm_smog_3_nm)
      23 -> data.icon(R.drawable.fnc_gpm_smog_4, R.drawable.fnc_gpm_smog_4_nm)
      24 -> data.icon(R.drawable.fnc_gpm_smog_5, R.drawable.fnc_gpm_smog_5_nm)
      25 -> data.icon(R.drawable.fnc_gpm_smog_6, R.drawable.fnc_gpm_smog_6_nm)
      26 -> data.icon(R.drawable.fnc_gpm_sound_1, R.drawable.fnc_gpm_sound_1_nm)
      27 -> data.icon(R.drawable.fnc_gpm_sound_2, R.drawable.fnc_gpm_sound_2_nm)
      28 -> data.icon(R.drawable.fnc_gpm_sound_3, R.drawable.fnc_gpm_sound_3_nm)
      29 -> data.icon(R.drawable.fnc_gpm_transfer, R.drawable.fnc_gpm_transfer_nm)
      30 -> data.icon(R.drawable.fnc_gpm_voltage_1, R.drawable.fnc_gpm_voltage_1_nm)
      31 -> data.icon(R.drawable.fnc_gpm_voltage_2, R.drawable.fnc_gpm_voltage_2_nm)
      else -> data.icon(R.drawable.fnc_gpm_5, R.drawable.fnc_gpm_5_nm)
    }
}
