package org.supla.android.usecases.channel
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

import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Switch may have value based on connected meter device (IC or EM).
 * For old servers we need to get this value based on SuplaValue of the device.
 * For new servers (proto version > 21) based on relations.
 * This use case tries to get the value based on SuplaValue. If not available then looks for meter child.
 */
@Singleton
class GetSwitchValueStringUseCase @Inject constructor(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase
) {
  operator fun invoke(
    channelWithChildren: ChannelWithChildren,
    valueType: ValueType = ValueType.FIRST,
    withUnit: Boolean = true
  ): String? =
    getChannelValueStringUseCase.valueOrNull(channelWithChildren.channel, valueType, withUnit)
      ?: getMeterChildValue(channelWithChildren, valueType, withUnit)

  private fun getMeterChildValue(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean) =
    channelWithChildren.children.firstOrNull { it.relationType == ChannelRelationType.METER }?.let {
      getChannelValueStringUseCase.valueOrNull(it.channelDataEntity, valueType, withUnit)
    }
}
