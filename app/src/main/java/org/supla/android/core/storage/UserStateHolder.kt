package org.supla.android.core.storage
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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.supla.android.Trace
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DefaultChartState
import org.supla.android.data.model.chart.ElectricityChartState
import org.supla.android.data.model.electricitymeter.ElectricityMeterSettings
import org.supla.android.extensions.TAG
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_STATE_PREFERENCES = "user_state_preferences"
private const val TEMPERATURE_CHART_STATE = "temperature_chart_state_%PROFILE_ID%_%CHANNEL_ID%"
private const val ELECTRICITY_METER_SETTINGS = "electricity_meter_settings_%PROFILE_ID%_%CHANNEL_ID%"
private const val OCR_PHOTO_CREATION_TIME = "ocr_photo_creation_time_%PROFILE_ID%_%CHANNEL_ID%"

@Singleton
class UserStateHolder @Inject constructor(@ApplicationContext context: Context) {

  private val preferences = context.getSharedPreferences(USER_STATE_PREFERENCES, Context.MODE_PRIVATE)

  fun getDefaultChartState(profileId: Long, remoteId: Int): DefaultChartState =
    preferences.getString(getKey(TEMPERATURE_CHART_STATE, profileId, remoteId), null)
      ?.let { DefaultChartState.from(it) } ?: DefaultChartState.default()

  fun getElectricityChartState(profileId: Long, remoteId: Int): ElectricityChartState =
    preferences.getString(getKey(TEMPERATURE_CHART_STATE, profileId, remoteId), null)
      ?.let { ElectricityChartState.from(it) } ?: ElectricityChartState.default()

  fun setChartState(state: ChartState, profileId: Long, remoteId: Int) {
    with(preferences.edit()) {
      putString(getKey(TEMPERATURE_CHART_STATE, profileId, remoteId), state.toJson())
      apply()
    }
  }

  fun getElectricityMeterSettings(profileId: Long, remoteId: Int): ElectricityMeterSettings =
    preferences.getString(getKey(ELECTRICITY_METER_SETTINGS, profileId, remoteId), null)
      ?.let { ElectricityMeterSettings.from(it) } ?: ElectricityMeterSettings.default()

  fun setElectricityMeterSettings(settings: ElectricityMeterSettings, profileId: Long, remoteId: Int) {
    with(preferences.edit()) {
      putString(getKey(ELECTRICITY_METER_SETTINGS, profileId, remoteId), Json.encodeToString(settings))
      apply()
    }
  }

  fun getOcrPhotoCreationTime(profileId: Long, remoteId: Int): Date? =
    preferences.getString(getKey(OCR_PHOTO_CREATION_TIME, profileId, remoteId), null)
      ?.let {
        try {
          Date.from(LocalDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC")).toInstant())
        } catch (ex: Exception) {
          Trace.e(TAG, "Could not parse ocr photo creation date", ex)
          null
        }
      }

  fun setOcrPhotoCreationTime(creationTime: String, profileId: Long, remoteId: Int) {
    with(preferences.edit()) {
      putString(getKey(OCR_PHOTO_CREATION_TIME, profileId, remoteId), creationTime)
      apply()
    }
  }

  private fun getKey(key: String, profileId: Long, remoteId: Int) =
    key.replace("%PROFILE_ID%", profileId.toString())
      .replace("%CHANNEL_ID%", remoteId.toString())
}
