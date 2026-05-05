package org.supla.android.data.model.settings
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

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.supla.android.R
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber

@Serializable
data class ImpulseCounterSettings(
  val showOnList: ListValue
) {
  companion object {
    fun default(): ImpulseCounterSettings =
      ImpulseCounterSettings(
        showOnList = ListValue.COUNTER_STATE
      )

    fun from(text: String): ImpulseCounterSettings? =
      try {
        Json.decodeFromString<ImpulseCounterSettings>(text)
      } catch (ex: SerializationException) {
        Timber.w(ex, "Could not restore impulse counter settings!")
        null
      }
  }
}

enum class ListValue(override val label: LocalizedString) : SpinnerItem {
  COUNTER_STATE(localizedString(R.string.impulse_counter_settings_counter_status)),
  LAST_24_HOURS(localizedString(R.string.history_range_last_day)),
  LAST_7_DAYS(localizedString(R.string.history_range_last_week)),
  LAST_30_DAYS(localizedString(R.string.history_range_last_30_days)),
  LAST_90_DAYS(localizedString(R.string.history_range_last_90_days)),
  LAST_365_DAYS(localizedString(R.string.history_range_last_365_days)),
  CURRENT_HOUR(localizedString(R.string.general_current_hour)),
  CURRENT_DAY(localizedString(R.string.general_current_day)),
  CURRENT_WEEK(localizedString(R.string.general_current_week)),
  CURRENT_MONTH(localizedString(R.string.general_current_month)),
  CURRENT_YEAR(localizedString(R.string.general_current_year))
}
