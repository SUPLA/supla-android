package org.supla.android.usecases.channel.valueprovider.parser

import org.supla.android.data.source.local.entity.ChannelValueEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface IntValueParser {

  fun asIntValue(channelValueEntity: ChannelValueEntity): Int? {
    return asIntValue(channelValueEntity, 0, 3)
  }

  /**
   * @param startPos - starting position in the byte array (starts from 0)
   * @param endPos - ending position in the byte array (starts from 0)
   */
  fun asIntValue(channelValueEntity: ChannelValueEntity, startPos: Int, endPos: Int): Int? {
    channelValueEntity.getValueAsByteArray().let {
      if (it.size <= endPos) {
        return null
      }

      return try {
        ByteBuffer
          .wrap(it.slice(startPos..endPos).toByteArray())
          .order(ByteOrder.LITTLE_ENDIAN)
          .getInt()
      } catch (exception: Exception) {
        null
      }
    }
  }
}
