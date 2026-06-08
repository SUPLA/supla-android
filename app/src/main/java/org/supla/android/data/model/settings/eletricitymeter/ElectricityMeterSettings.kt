package org.supla.android.data.model.settings.eletricitymeter
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
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.ListValueAggregation.NO_AGGREGATION
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber

@Serializable
data class ElectricityMeterSettings(
  val currentMonthBalancing: ElectricityMeterBalanceType,
  val metricOnList: ElectricityMeterMeasurementType,
  val metricOnListBalancing: ElectricityMeterBalanceType,
  val metricOnListAggregation: ListValueAggregation
) {

  val usingAggregatedValue: Boolean
    get() = metricOnList.isBalance || (metricOnList.aggregationAvailable && metricOnListAggregation != NO_AGGREGATION)

  companion object {
    fun default(): ElectricityMeterSettings =
      ElectricityMeterSettings(
        metricOnList = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY,
        currentMonthBalancing = ElectricityMeterBalanceType.DEFAULT,
        metricOnListBalancing = ElectricityMeterBalanceType.ARITHMETIC,
        metricOnListAggregation = NO_AGGREGATION
      )

    fun from(text: String): ElectricityMeterSettings? =
      try {
        Json.decodeFromString<ElectricityMeterSettings>(text)
      } catch (ex: SerializationException) {
        Timber.w(ex, "Could not restore electricity meter settings!")
        // Check previous version
        ElectricityMeterSettingsV1.from(text)?.settings
      }

    val balancingAllItems = listOf(
      ElectricityMeterBalanceType.VECTOR,
      ElectricityMeterBalanceType.ARITHMETIC,
      ElectricityMeterBalanceType.HOURLY
    )
  }
}

@Serializable
private data class ElectricityMeterSettingsV1(
  val showOnList: SuplaElectricityMeasurementType,
  val balancing: ElectricityMeterBalanceType
) {

  val settings: ElectricityMeterSettings
    get() = ElectricityMeterSettings(
      metricOnList = ElectricityMeterMeasurementType.from(showOnList),
      currentMonthBalancing = balancing,
      metricOnListBalancing = ElectricityMeterBalanceType.ARITHMETIC,
      metricOnListAggregation = NO_AGGREGATION
    )

  companion object {
    fun from(text: String): ElectricityMeterSettingsV1? =
      try {
        Json.decodeFromString<ElectricityMeterSettingsV1>(text)
      } catch (ex: SerializationException) {
        Timber.w(ex, "Could not restore electricity meter settings (v1)!")
        null
      }
  }
}

enum class ElectricityMeterBalanceType(override val label: LocalizedString) : SpinnerItem {
  DEFAULT(LocalizedString.Empty),
  VECTOR(localizedString(R.string.details_em_balance_vector)),
  ARITHMETIC(localizedString(R.string.details_em_balance_arithmetic)),
  HOURLY(localizedString(R.string.details_em_balance_hourly))
}
