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
import org.supla.android.data.source.local.entity.complex.ImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.isImpulseCounter
import org.supla.android.data.source.local.entity.isSwitch
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.SwitchWithElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.SwitchWithImpulseCounterValueProvider
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ImpulseCounterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchWithMeterValueStringProvider @Inject constructor(
  private val switchWithElectricityMeterValueProvider: SwitchWithElectricityMeterValueProvider,
  private val switchWithImpulseCounterValueProvider: SwitchWithImpulseCounterValueProvider,
  private val impulseCounterValueStringProvider: ImpulseCounterValueStringProvider,
  private val userStateHolder: UserStateHolder
) : ChannelValueStringProvider {

  private val emFormatter = ElectricityMeterValueFormatter()
  private val icFormatter = ImpulseCounterValueFormatter()

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean {
    val channel = channelWithChildren.channel
    return channel.isSwitch() && (channelWithChildren.isOrHasElectricityMeter || channelWithChildren.isOrHasImpulseCounter)
  }

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean): String? {
    val channelData = channelWithChildren.channel
    val meterChild = channelWithChildren.children.firstOrNull { it.relationType == ChannelRelationType.METER }

    if (channelWithChildren.isOrHasElectricityMeter) {
      val value = switchWithElectricityMeterValueProvider.value(channelWithChildren, valueType)
      val type = userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId).showOnListSafe

      return if (type == SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY) {
        emFormatter.format(value, withUnit(withUnit))
      } else {
        emFormatter.format(
          value = value,
          format = ValueFormat(
            withUnit = withUnit,
            customUnit = " ${type.unit}",
            showNoValueText = false
          )
        )
      }
    }

    if (meterChild?.channel?.isImpulseCounter() == true) {
      return impulseCounterValueStringProvider.value(meterChild.withChildren, valueType, withUnit)
    }

    if (channelData.channelValueEntity.subValueType == SUBV_TYPE_IC_MEASUREMENTS.toShort()) {
      val value = switchWithImpulseCounterValueProvider.value(channelWithChildren, valueType)
      return icFormatter.format(
        value = value,
        format = withUnit(withUnit = withUnit, unit = channelData.ImpulseCounter.value?.unit)
      )
    }

    return null
  }
}
