package org.supla.android.usecases.channel.valueprovider.parser

import org.supla.android.data.source.local.entity.ChannelValueEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface IntValueParser {

  /**
   * @param startPos - starting position in the byte array (starts from 0)
   * @param endPos - ending position in the byte array (starts from 0)
   */
  fun asIntValue(channelValueEntity: ChannelValueEntity, startPos: Int = 0, endPos: Int = 3): Int? =
    asIntValue(channelValueEntity.getValueAsByteArray(), startPos, endPos)

  /**
   * @param startPos - starting position in the byte array (starts from 0)
   * @param endPos - ending position in the byte array (starts from 0)
   */
  fun asIntValue(byteArray: ByteArray, startPos: Int = 0, endPos: Int = 3): Int? {
    byteArray.let {
      if (it.size <= endPos) {
        return null
      }

      return try {
        ByteBuffer
          .wrap(it.slice(startPos..endPos).toByteArray())
          .order(ByteOrder.LITTLE_ENDIAN)
          .getInt()
      } catch (_: Exception) {
        null
      }
    }
  }
}
