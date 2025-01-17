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
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.usecases.channel.ChannelValueProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.parser.IntValueParser
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectricityMeterValueProvider @Inject constructor(
  private val userStateHolder: UserStateHolder
) : ChannelValueProvider, IntValueParser {

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean =
    channelWithChildren.function == SuplaFunction.ELECTRICITY_METER

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType): Any =
    when (userStateHolder.getElectricityMeterSettings(channelWithChildren.profileId, channelWithChildren.remoteId).showOnListSafe) {
      SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY ->
        channelWithChildren.channel.Electricity.value?.summary?.totalReverseActiveEnergy ?: UNKNOWN_VALUE

      SuplaElectricityMeasurementType.POWER_ACTIVE ->
        channelWithChildren.channel.Electricity.value?.let { value ->
          val powerActive = Phase.entries
            .filter { it.disabledFlag.rawValue and channelWithChildren.flags == 0L }
            .mapNotNull { value.getMeasurement(it.value, 0)?.powerActive }
            .let { if (it.isEmpty()) Double.NaN else it.sum() }

          if (value.measuredValues.suplaElectricityMeterMeasuredTypes.contains(SuplaElectricityMeasurementType.POWER_ACTIVE_KW)) {
            powerActive.times(1000)
          } else {
            powerActive
          }
        } ?: UNKNOWN_VALUE

      SuplaElectricityMeasurementType.VOLTAGE ->
        channelWithChildren.channel.Electricity.value?.let { value ->
          Phase.entries
            .filter { it.disabledFlag.rawValue and channelWithChildren.flags == 0L }
            .mapNotNull { value.getMeasurement(it.value, 0)?.voltage }
            .average()
        } ?: UNKNOWN_VALUE

      else -> asIntValue(channelWithChildren.channel.channelValueEntity, startPos = 1, endPos = 4)?.div(100.0) ?: UNKNOWN_VALUE
    }

  companion object {
    const val UNKNOWN_VALUE = 0.0
  }
}
