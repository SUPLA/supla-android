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
import org.supla.android.lib.actions.SubjectType

class SubjectTypeAdapter : TypeAdapter<SubjectType>() {
  override fun read(reader: JsonReader): SubjectType? = when (reader.peek()) {
    JsonToken.NULL -> {
      reader.nextNull()
      null
    }
    JsonToken.NUMBER -> runCatching { SubjectType.from(reader.nextInt()) }.getOrNull()
    JsonToken.STRING -> parse(reader.nextString())
    else -> {
      reader.skipValue()
      null
    }
  }

  override fun write(writer: JsonWriter, value: SubjectType?) {
    when (value) {
      null -> writer.nullValue()
      SubjectType.CHANNEL -> writer.value("channel")
      SubjectType.GROUP -> writer.value("channelGroup")
      SubjectType.SCENE -> writer.value("scene")
    }
  }

  private fun parse(value: String): SubjectType? = when (value) {
    "channel" -> SubjectType.CHANNEL
    "channelGroup" -> SubjectType.GROUP
    "scene" -> SubjectType.SCENE
    else -> null
  }
}
