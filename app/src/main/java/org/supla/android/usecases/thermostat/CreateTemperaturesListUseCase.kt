package org.supla.android.usecases.thermostat
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
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.features.details.thermostatdetail.general.MeasurementValue
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateTemperaturesListUseCase @Inject constructor(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase
) {

  operator fun invoke(channelWithChildren: ChannelWithChildren): List<MeasurementValue> =
    mutableListOf<MeasurementValue>().apply {
      val sortedChildren = channelWithChildren.children
        .filter { it.relationType.isThermometer() }
        .sortedBy { item -> item.relationType.value }

      if (sortedChildren.none { it.relationType.isMainThermometer() }) {
        add(
          MeasurementValue(
            remoteId = -1,
            imageId = ImageId(R.drawable.ic_unknown_channel),
            value = ValuesFormatter.NO_VALUE_TEXT
          )
        )
      }

      for (child in sortedChildren) {
        add(getTemperatureValue(child.channelDataEntity))
        if (child.function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          add(getHumidityValue(child.channelDataEntity))
        }
      }
    }

  private fun getTemperatureValue(channelData: ChannelDataEntity): MeasurementValue =
    MeasurementValue(
      remoteId = channelData.remoteId,
      imageId = getChannelIconUseCase(channelData),
      value = getChannelValueStringUseCase(channelData, withUnit = false)
    )

  private fun getHumidityValue(channelData: ChannelDataEntity): MeasurementValue =
    MeasurementValue(
      remoteId = channelData.remoteId,
      imageId = getChannelIconUseCase(channelData, IconType.SECOND),
      value = getChannelValueStringUseCase(channelData, ValueType.SECOND, withUnit = false)
    )
}
