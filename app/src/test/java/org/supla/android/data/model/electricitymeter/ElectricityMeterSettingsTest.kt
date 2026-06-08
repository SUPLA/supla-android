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

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings

class ElectricityMeterSettingsTest {
  @Test
  fun `should convert to JSON and back`() {
    // given
    val settings = ElectricityMeterSettings.default().copy(metricOnList = ElectricityMeterMeasurementType.VOLTAGE)

    // when
    val jsonString = Json.encodeToString(settings)
    val result = ElectricityMeterSettings.from(jsonString)

    // then
    assertThat(result).isEqualTo(settings)
    assertThat(settings.metricOnList).isEqualTo(ElectricityMeterMeasurementType.VOLTAGE)
  }

  @Test
  fun `should get correct type and balancing`() {
    // when
    val settings = ElectricityMeterSettings.default().copy(
      metricOnList = ElectricityMeterMeasurementType.VOLTAGE,
      currentMonthBalancing = ElectricityMeterBalanceType.HOURLY
    )

    // then
    assertThat(settings.metricOnList).isEqualTo(ElectricityMeterMeasurementType.VOLTAGE)
    assertThat(settings.currentMonthBalancing).isEqualTo(ElectricityMeterBalanceType.HOURLY)
  }

  @Test
  fun `should get type from legacy JSON string`() {
    // given
    val string = """{"showOnList" : "POWER_ACTIVE", "balancing" : "HOURLY"}"""

    // when
    val result = ElectricityMeterSettings.from(string)

    // then
    assertThat(result).isNotNull
    assertThat(result?.metricOnList).isEqualTo(ElectricityMeterMeasurementType.POWER_ACTIVE)
    assertThat(result?.metricOnListAggregation).isEqualTo(ListValueAggregation.NO_AGGREGATION)
    assertThat(result?.metricOnListBalancing).isEqualTo(ElectricityMeterBalanceType.ARITHMETIC)
    assertThat(result?.currentMonthBalancing).isEqualTo(ElectricityMeterBalanceType.HOURLY)
  }

  @Test
  fun `should get null when could not decode from string`() {
    // given
    val string = "Wrong format"

    // when
    val result = ElectricityMeterSettings.from(string)

    // then
    assertThat(result).isNull()
  }
}
