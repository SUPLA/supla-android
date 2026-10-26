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

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import org.supla.android.R
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import java.time.LocalDateTime

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
  @SerializedName("state") val state: ScheduleStateDto? = null,
  @SerializedName("closestExecutions") val closestExecutions: ScheduleClosestExecutionsDto? = null
) {
  val captionString: LocalizedString
    get() = caption
      ?.takeIf { it.isNotBlank() }
      ?.let { LocalizedString.Constant(it) } ?: localizedString(R.string.schedule_default_caption, id ?: 0)
}

data class ScheduleClosestExecutionsDto(
  @SerializedName("past") val past: List<ScheduleClosestExecutionDto>? = null,
  @SerializedName("future") val future: List<ScheduleClosestExecutionDto>? = null
)

data class ScheduleClosestExecutionDto(
  @SerializedName("plannedTimestamp")
  @JsonAdapter(IsoLocalDateTimeAdapter::class)
  val plannedTimestamp: LocalDateTime? = null,
  @SerializedName("failed") val failed: Boolean? = null
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
  @SerializedName("activeHours") val activeHours: Map<String, List<Int>>? = null,
  @SerializedName("activityConditions") val activityConditions: List<List<ScheduleActivityConditionDto>>? = null,
  @SerializedName("subDeviceId") val subDeviceId: Int? = null,
  @SerializedName("conflictDetails") val conflictDetails: ScheduleSubjectConflictDetailsDto? = null,
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
  @SerializedName("config") val config: ScheduleSubjectConfigDto? = null
)

/** A value-based trigger can define one or more alternative sunrise/sunset time ranges. */
data class ScheduleActivityConditionDto(
  @SerializedName("afterSunrise") val afterSunrise: Int? = null,
  @SerializedName("beforeSunrise") val beforeSunrise: Int? = null,
  @SerializedName("afterSunset") val afterSunset: Int? = null,
  @SerializedName("beforeSunset") val beforeSunset: Int? = null
)

/** Cloud currently exposes these channel conflict details to the client. */
data class ScheduleSubjectConflictDetailsDto(
  @SerializedName("missing") val missing: Boolean? = null,
  @SerializedName("type") val type: Int? = null
)

/**
 * Public subject config is a union of function-specific Cloud configs. These are the common
 * properties exposed by Cloud for channels and scenes; unknown function-specific properties are
 * ignored by Gson because this DTO is only used as schedule subject metadata.
 */
data class ScheduleSubjectConfigDto(
  @SerializedName("alexa") val alexa: ScheduleSubjectAlexaConfigDto? = null,
  @SerializedName("googleHome") val googleHome: ScheduleSubjectGoogleHomeConfigDto? = null,
  @SerializedName("isBatteryAvailable") val isBatteryAvailable: Boolean? = null,
  @SerializedName("availableRgbwCommands") val availableRgbwCommands: List<String>? = null
)

data class ScheduleSubjectAlexaConfigDto(
  @SerializedName("alexaDisabled") val alexaDisabled: Boolean? = null
)

data class ScheduleSubjectGoogleHomeConfigDto(
  @SerializedName("googleHomeDisabled") val googleHomeDisabled: Boolean? = null,
  @SerializedName("needsUserConfirmation") val needsUserConfirmation: Boolean? = null,
  @SerializedName("pin") val pin: String? = null,
  @SerializedName("pinSet") val pinSet: Boolean? = null
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
