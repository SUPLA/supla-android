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

import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.guardLet
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.WeightSensorValueProvider
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class WeightSensorValueStringProvider @Inject constructor(
  private val weightSensorValueProvider: WeightSensorValueProvider
) : ChannelValueStringProvider {
  val formatter: DecimalFormat = DecimalFormat().apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean =
    weightSensorValueProvider.handle(channelWithChildren)

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean): String {
    val (doubleValue) = guardLet(weightSensorValueProvider.value(channelWithChildren, valueType) as? Double) {
      return ValuesFormatter.NO_VALUE_TEXT
    }
    if (doubleValue <= WeightSensorValueProvider.UNKNOWN_VALUE) {
      return ValuesFormatter.NO_VALUE_TEXT
    }

    return if (withUnit) {
      if (abs(doubleValue) >= 2000) {
        "${formatter.format(doubleValue / 1000)} kg"
      } else {
        "${doubleValue.toInt()} g"
      }
    } else {
      "${doubleValue.toInt()}"
    }
  }
}
