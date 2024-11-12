package org.supla.android.data.source.remote
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

import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.core.shared.data.model.general.SuplaFunction

enum class ChannelConfigType(val value: Int) {
  UNKNOWN(-1),
  DEFAULT(0),
  WEEKLY_SCHEDULE(2),
  GENERAL_PURPOSE_MEASUREMENT(3),
  GENERAL_PURPOSE_METER(4),
  FACADE_BLIND(5);

  companion object {
    fun from(value: Int): ChannelConfigType {
      for (type in entries) {
        if (type.value == value) {
          return type
        }
      }

      return UNKNOWN
    }

    fun from(channel: ChannelEntity) =
      when (channel.function) {
        SuplaFunction.GENERAL_PURPOSE_MEASUREMENT -> GENERAL_PURPOSE_MEASUREMENT
        SuplaFunction.GENERAL_PURPOSE_METER -> GENERAL_PURPOSE_METER

        SuplaFunction.VERTICAL_BLIND,
        SuplaFunction.CONTROLLING_THE_FACADE_BLIND -> FACADE_BLIND

        else -> throw IllegalArgumentException("Channel not supported (function: `${channel.function}`)")
      }
  }
}

open class SuplaChannelConfig(
  @Transient open val remoteId: Int,
  @Transient open val func: Int?,
  @Transient open val crc32: Long
)
