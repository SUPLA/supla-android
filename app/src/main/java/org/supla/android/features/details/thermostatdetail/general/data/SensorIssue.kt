package org.supla.android.features.details.thermostatdetail.general.data
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.thermostat.ThermostatValue

data class SensorIssue(
  val imageId: ImageId?,
  val textProvider: StringProvider
) {

  companion object {
    fun build(value: ThermostatValue, children: List<ChannelChildEntity>, getChannelIconUseCase: GetChannelIconUseCase): SensorIssue? {
      if (value.flags.contains(SuplaThermostatFlag.FORCED_OFF_BY_SENSOR).not()) {
        return null
      }

      return children.firstOrNull { it.relationType == ChannelRelationType.DEFAULT }?.let {
        SensorIssue(
          imageId = getChannelIconUseCase(it.channelDataEntity),
          textProvider = { context ->
            when (it.function) {
              SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW,
              SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW -> context.getString(R.string.thermostat_detail_off_by_window)

              SUPLA_CHANNELFNC_HOTELCARDSENSOR -> context.getString(R.string.thermostat_detail_off_by_card)

              else -> context.getString(R.string.thermostat_detail_off_by_sensor)
            }
          }
        )
      } ?: SensorIssue(
        imageId = null,
        textProvider = { it.getString(R.string.thermostat_detail_off_by_sensor) }
      )
    }
  }
}
