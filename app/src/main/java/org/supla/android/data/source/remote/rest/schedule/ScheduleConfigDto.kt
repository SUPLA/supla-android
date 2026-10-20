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
import com.google.gson.annotations.SerializedName

/** One schedule entry from the API's config array. */
data class ScheduleConfigDto(
  @SerializedName("crontab") val crontab: String? = null,
  @SerializedName("date") val date: String? = null,
  @SerializedName("time") val time: String? = null,
  @SerializedName("dayOfWeek") val dayOfWeek: Int? = null,
  @SerializedName("action") val action: ScheduleConfigActionDto? = null
)

data class ScheduleConfigActionDto(
  @SerializedName("id") val id: Int? = null,
  @SerializedName("param") val param: JsonElement? = null
)
