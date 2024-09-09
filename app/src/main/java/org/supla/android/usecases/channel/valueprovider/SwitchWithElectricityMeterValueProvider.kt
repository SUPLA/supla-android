package org.supla.android.usecases.channel.valueprovider
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

import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.usecases.channel.ChannelValueProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.parser.IntValueParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchWithElectricityMeterValueProvider @Inject constructor(
  private val userStateHolder: UserStateHolder,
  private val electricityMeterValueProvider: ElectricityMeterValueProvider
) : ChannelValueProvider, IntValueParser {

  override fun handle(channelData: ChannelDataEntity): Boolean =
    when (channelData.function) {
      SUPLA_CHANNELFNC_POWERSWITCH,
      SUPLA_CHANNELFNC_LIGHTSWITCH,
      SUPLA_CHANNELFNC_STAIRCASETIMER ->
        channelData.channelValueEntity.subValueType == SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()

      else -> false
    }

  override fun value(channelData: ChannelDataEntity, valueType: ValueType): Any =
    when (userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId).showOnListSafe) {
      SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY,
      SuplaElectricityMeasurementType.POWER_ACTIVE,
      SuplaElectricityMeasurementType.VOLTAGE ->
        electricityMeterValueProvider.value(channelData, valueType)

      else -> asIntValue(
        channelData.channelValueEntity.getSubValueAsByteArray(),
        startPos = 1,
        endPos = 4
      )?.div(100.0) ?: UNKNOWN_VALUE
    }

  companion object {
    const val UNKNOWN_VALUE = 0.0
  }
}
