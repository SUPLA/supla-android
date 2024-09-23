package org.supla.android.usecases.channel.valueprovider.parser
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

import org.supla.android.data.source.local.entity.ChannelValueEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface LongValueProvider {
  /**
   * @param startPos - starting position in the byte array (starts from 0)
   * @param endPos - ending position in the byte array (starts from 0)
   */
  fun asLongValue(channelValueEntity: ChannelValueEntity, startPos: Int = 0, endPos: Int = 7): Long? =
    asLongValue(channelValueEntity.getValueAsByteArray(), startPos, endPos)

  /**
   * @param startPos - starting position in the byte array (starts from 0)
   * @param endPos - ending position in the byte array (starts from 0)
   */
  fun asLongValue(byteArray: ByteArray, startPos: Int = 0, endPos: Int = 7): Long? {
    byteArray.let {
      if (it.size <= endPos) {
        return null
      }

      return try {
        ByteBuffer
          .wrap(it.slice(startPos..endPos).toByteArray())
          .order(ByteOrder.LITTLE_ENDIAN)
          .getLong()
      } catch (exception: Exception) {
        null
      }
    }
  }
}
