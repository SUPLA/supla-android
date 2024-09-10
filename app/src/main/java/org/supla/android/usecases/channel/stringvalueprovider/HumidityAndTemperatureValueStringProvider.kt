package org.supla.android.usecases.channel.stringvalueprovider
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

import org.supla.android.Preferences
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.HumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.channel.valueprovider.HumidityAndTemperatureValueProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumidityAndTemperatureValueStringProvider @Inject constructor(
  private val humidityAndTemperatureValueProvider: HumidityAndTemperatureValueProvider,
  preferences: Preferences
) : ChannelValueStringProvider {

  private val temperatureFormatter = ThermometerValueFormatter(preferences)
  private val humidityFormatter = HumidityValueFormatter()
  override fun handle(channelData: ChannelDataEntity): Boolean =
    channelData.function == SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

  override fun value(channelData: ChannelDataEntity, valueType: ValueType, withUnit: Boolean): String {
    val value = humidityAndTemperatureValueProvider.value(channelData, valueType)
    return when (valueType) {
      ValueType.FIRST -> temperatureFormatter.format(value, withUnit = withUnit)
      ValueType.SECOND -> humidityFormatter.format(value, withUnit = withUnit)
    }
  }
}
