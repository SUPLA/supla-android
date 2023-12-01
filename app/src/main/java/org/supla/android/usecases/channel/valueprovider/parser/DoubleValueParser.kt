package org.supla.android.usecases.channel.valueprovider.parser

import org.supla.android.data.source.local.entity.ChannelValueEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface DoubleValueParser {

  fun asDoubleValue(channelValueEntity: ChannelValueEntity): Double? {
    return channelValueEntity.getValueAsByteArray().let {
      if (it.isEmpty()) {
        null
      }

      try {
        ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).getDouble()
      } catch (exception: Exception) {
        null
      }
    }
  }
}
