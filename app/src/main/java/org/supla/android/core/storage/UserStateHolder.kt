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
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.data.model.chart.TemperatureChartState
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_STATE_PREFERENCES = "user_state_preferences"
private const val TEMPERATURE_CHART_STATE = "temperature_chart_state_%PROFILE_ID%_%CHANNEL_ID%"

@Singleton
class UserStateHolder @Inject constructor(@ApplicationContext context: Context) {

  private val preferences = context.getSharedPreferences(USER_STATE_PREFERENCES, Context.MODE_PRIVATE)
  private val gson = GsonBuilder().create()

  fun getChartState(profileId: Long, remoteId: Int) =
    preferences.getString(getKey(TEMPERATURE_CHART_STATE, profileId, remoteId), null)?.let {
      gson.fromJson(it, TemperatureChartState::class.java)
    } ?: TemperatureChartState.default()

  fun setChartState(state: TemperatureChartState, profileId: Long, remoteId: Int) {
    with(preferences.edit()) {
      putString(getKey(TEMPERATURE_CHART_STATE, profileId, remoteId), gson.toJson(state))
      apply()
    }
  }

  private fun getKey(key: String, profileId: Long, remoteId: Int) =
    key.replace("%PROFILE_ID%", profileId.toString())
      .replace("%CHANNEL_ID%", remoteId.toString())
}
