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

import org.supla.android.data.ValuesFormatter.Companion.NO_VALUE_TEXT
import org.supla.android.data.source.local.entity.complex.ImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.ImpulseCounterValueFormatter
import org.supla.android.usecases.channel.valueprovider.ImpulseCounterValueProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpulseCounterValueStringProvider @Inject constructor(
  private val impulseCounterValueProvider: ImpulseCounterValueProvider
) : ChannelValueStringProvider {

  val formatter = ImpulseCounterValueFormatter()

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean =
    impulseCounterValueProvider.handle(channelWithChildren.channel)

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean): String {
    val channelData = channelWithChildren.channel
    val value = impulseCounterValueProvider.value(channelData, valueType)

    if (value == ImpulseCounterValueProvider.UNKNOWN_VALUE) {
      return NO_VALUE_TEXT
    }

    val unit = channelData.ImpulseCounter.value?.unit?.let { ImpulseCounterValueFormatter.Data(it) }
    return formatter.format(value, withUnit, custom = unit)
  }
}
