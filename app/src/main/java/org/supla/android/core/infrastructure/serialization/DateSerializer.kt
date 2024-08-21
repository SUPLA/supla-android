package org.supla.android.core.infrastructure.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

class DateSerializer : KSerializer<Date> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

  override fun deserialize(decoder: Decoder): Date = Date(decoder.decodeLong())

  override fun serialize(encoder: Encoder, value: Date) {
    encoder.encodeLong(value.time)
  }
}
