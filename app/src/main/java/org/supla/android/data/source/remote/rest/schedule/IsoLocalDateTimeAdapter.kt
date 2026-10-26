package org.supla.android.data.source.remote.rest.schedule
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

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class IsoLocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
  override fun read(reader: JsonReader): LocalDateTime? = when (reader.peek()) {
    JsonToken.NULL -> {
      reader.nextNull()
      null
    }
    else -> {
      val timestamp = reader.nextString()
      runCatching {
        OffsetDateTime.parse(timestamp)
          .atZoneSameInstant(ZoneId.systemDefault())
          .toLocalDateTime()
      }.getOrElse { LocalDateTime.parse(timestamp) }
    }
  }

  override fun write(writer: JsonWriter, value: LocalDateTime?) {
    if (value == null) {
      writer.nullValue()
    } else {
      writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }
  }
}
