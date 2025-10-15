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

import org.supla.android.data.source.local.entity.complex.ImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.ImpulseCounterValueProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ImpulseCounterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpulseCounterValueStringProvider @Inject constructor(
  private val impulseCounterValueProvider: ImpulseCounterValueProvider
) : ChannelValueStringProvider {

  private val impulseCounterFormatter = ImpulseCounterValueFormatter()
  private val electricityMeterFormatter = ElectricityMeterValueFormatter()

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean =
    impulseCounterValueProvider.handle(channelWithChildren)

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean): String {
    val channelData = channelWithChildren.channel
    val value = impulseCounterValueProvider.value(channelWithChildren, valueType)

    return if (channelWithChildren.function == SuplaFunction.IC_ELECTRICITY_METER) {
      electricityMeterFormatter.format(value, ValueFormat(withUnit))
    } else {
      impulseCounterFormatter.format(
        value = value,
        format = withUnit(withUnit = withUnit, unit = channelData.ImpulseCounter.value?.unit)
      )
    }
  }
}
