package org.supla.android.usecases.channel.valueprovider

import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.ChannelValueProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueprovider.parser.IntValueParser

abstract class DefaultIntValueProvider : ChannelValueProvider, IntValueParser {

  abstract val unknownValue: Int

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType): Int {
    return asIntValue(channelWithChildren.channel.channelValueEntity) ?: unknownValue
  }
}
