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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.supla.android.lib.actions.SubjectType

/** Schedule representation returned by SUPLA Cloud API v3. */
data class ScheduleDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("caption") val caption: String? = null,
  @SerializedName("subjectType")
  @JsonAdapter(SubjectTypeAdapter::class)
  val subjectType: SubjectType? = null,
  @SerializedName("subjectId") val subjectId: Int? = null,
  @SerializedName("subject") val subject: ScheduleSubjectDto? = null,
  @SerializedName("enabled") val enabled: Boolean? = null,
  @SerializedName("dateStart") val dateStart: String? = null,
  @SerializedName("dateEnd") val dateEnd: String? = null,
  @SerializedName("retry") val retry: Boolean? = null,
  @SerializedName("mode") val mode: String? = null,
  @SerializedName("config") val config: List<ScheduleConfigDto>? = null,
  @SerializedName("state") val state: ScheduleStateDto? = null
)

data class ScheduleSubjectDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("channelNumber") val channelNumber: Int? = null,
  @SerializedName("caption") val caption: String? = null,
  @SerializedName("type") val type: ScheduleSubjectTypeDto? = null,
  @SerializedName("function") val function: ScheduleSubjectFunctionDto? = null,
  @SerializedName("altIcon") val altIcon: Int? = null,
  @SerializedName("enabled") val enabled: Boolean? = null,
  @SerializedName("hidden") val hidden: Boolean? = null,
  @SerializedName("estimatedExecutionTime") val estimatedExecutionTime: Long? = null,
  @SerializedName("activeFrom") val activeFrom: String? = null,
  @SerializedName("activeTo") val activeTo: String? = null,
  @SerializedName("activeHours") val activeHours: JsonElement? = null,
  @SerializedName("activityConditions") val activityConditions: JsonElement? = null,
  @SerializedName("subDeviceId") val subDeviceId: Int? = null,
  @SerializedName("conflictDetails") val conflictDetails: JsonObject? = null,
  @SerializedName("checksum") val checksum: String? = null,
  @SerializedName("isVirtual") val isVirtual: Boolean? = null,
  @SerializedName("ownSubjectType") val ownSubjectType: String? = null,
  @SerializedName("inheritedLocation") val inheritedLocation: Boolean? = null,
  @SerializedName("possibleActions") val possibleActions: List<ScheduleSubjectActionDto>? = null,
  @SerializedName("deletable") val deletable: Boolean? = null,
  @SerializedName("userId") val userId: Int? = null,
  @SerializedName("iodeviceId") val iodeviceId: Int? = null,
  @SerializedName("locationId") val locationId: Int? = null,
  @SerializedName("functionId") val functionId: Int? = null,
  @SerializedName("userIconId") val userIconId: Int? = null,
  @SerializedName("typeId") val typeId: Int? = null,
  @SerializedName("config") val config: JsonObject? = null
)

data class ScheduleSubjectTypeDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("name") val name: String? = null,
  @SerializedName("caption") val caption: String? = null
)

data class ScheduleSubjectFunctionDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("name") val name: String? = null,
  @SerializedName("caption") val caption: String? = null,
  @SerializedName("possibleVisualStates") val possibleVisualStates: List<String>? = null,
  @SerializedName("maxAlternativeIconIndex") val maxAlternativeIconIndex: Int? = null,
  @SerializedName("output") val output: Boolean? = null
)

data class ScheduleSubjectActionDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("name") val name: String? = null,
  @SerializedName("nameSlug") val nameSlug: String? = null,
  @SerializedName("caption") val caption: String? = null
)

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
