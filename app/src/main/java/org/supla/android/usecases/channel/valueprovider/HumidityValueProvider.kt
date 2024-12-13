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

import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.usecases.channel.ChannelValueProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.parser.IntValueParser
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumidityValueProvider @Inject constructor() : IntValueParser, ChannelValueProvider {

  override fun handle(channelData: ChannelDataEntity): Boolean =
    channelData.function == SuplaFunction.HUMIDITY

  override fun value(channelData: ChannelDataEntity, valueType: ValueType): Any {
    return asIntValue(channelData.channelValueEntity, startPos = 4, endPos = 7)?.div(1000.0) ?: UNKNOWN_VALUE
  }

  companion object {
    const val UNKNOWN_VALUE = -1.0
  }
}
