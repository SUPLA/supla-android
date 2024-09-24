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
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.android.usecases.channel.valueprovider.SwitchWithElectricityMeterValueProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchWithElectricityMeterValueStringProvider @Inject constructor(
  private val switchWithElectricityMeterValueProvider: SwitchWithElectricityMeterValueProvider,
  private val userStateHolder: UserStateHolder
) : ChannelValueStringProvider {

  private val formatter = ListElectricityMeterValueFormatter()

  override fun handle(channelData: ChannelDataEntity): Boolean =
    switchWithElectricityMeterValueProvider.handle(channelData)

  override fun value(channelData: ChannelDataEntity, valueType: ValueType, withUnit: Boolean): String {
    val value = switchWithElectricityMeterValueProvider.value(channelData, valueType)
    val type = userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId).showOnListSafe
    return formatter.format(value, withUnit = withUnit, custom = type)
  }
}