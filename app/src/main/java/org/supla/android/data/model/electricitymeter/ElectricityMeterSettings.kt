package org.supla.android.data.model.electricitymeter
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
import org.supla.android.Trace
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.TAG
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

@Serializable
data class ElectricityMeterSettings(
  val showOnList: SuplaElectricityMeasurementType,
  val balancing: ElectricityMeterBalanceType
) {

  val showOnListSafe: SuplaElectricityMeasurementType
    get() = if (showOnListAllItems.contains(showOnList)) showOnList else showOnListAllItems.first()

  companion object {
    fun default(): ElectricityMeterSettings =
      ElectricityMeterSettings(
        showOnList = SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
        balancing = ElectricityMeterBalanceType.DEFAULT
      )

    fun from(text: String): ElectricityMeterSettings? =
      try {
        Json.decodeFromString<ElectricityMeterSettings>(text)
      } catch (ex: SerializationException) {
        Trace.w(TAG, "Could not restore chart state!", ex)
        null
      }

    val showOnListAllItems = listOf(
      SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY,
      SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY,
      SuplaElectricityMeasurementType.POWER_ACTIVE,
      SuplaElectricityMeasurementType.POWER_ACTIVE_KW,
      SuplaElectricityMeasurementType.VOLTAGE
    )

    val balancingAllItems = listOf(
      ElectricityMeterBalanceType.VECTOR,
      ElectricityMeterBalanceType.ARITHMETIC,
      ElectricityMeterBalanceType.HOURLY
    )
  }
}

enum class ElectricityMeterBalanceType(override val label: LocalizedString) : SpinnerItem {
  DEFAULT(LocalizedString.Empty),
  VECTOR(localizedString(R.string.details_em_balance_vector)),
  ARITHMETIC(localizedString(R.string.details_em_balance_arithmetic)),
  HOURLY(localizedString(R.string.details_em_balance_hourly))
}
