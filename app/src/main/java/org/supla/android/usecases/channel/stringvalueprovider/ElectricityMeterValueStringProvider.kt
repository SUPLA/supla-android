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

import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectricityMeterValueStringProvider @Inject constructor(
  private val electricityMeterValueProvider: ElectricityMeterValueProvider,
  private val userStateHolder: UserStateHolder
) : ChannelValueStringProvider {

  private val formatter = ListElectricityMeterValueFormatter()

  override fun handle(function: Int): Boolean =
    when (function) {
      SUPLA_CHANNELFNC_ELECTRICITY_METER,
      SUPLA_CHANNELFNC_POWERSWITCH,
      SUPLA_CHANNELFNC_LIGHTSWITCH,
      SUPLA_CHANNELFNC_STAIRCASETIMER -> true

      else -> false
    }

  override fun value(channelData: ChannelDataEntity, valueType: ValueType, withUnit: Boolean): String {
    val value = electricityMeterValueProvider.value(channelData, valueType)
    formatter.unit = userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId).showOnListSafe.unit
    return formatter.format(value, withUnit = withUnit)
  }
}
