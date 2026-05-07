package org.supla.android.usecases.channel.stringvalueprovider
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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.DefaultFirstValue
import org.supla.android.usecases.channel.ListFirstValue
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

class ElectricityMeterValueStringProviderTest {

  @MockK
  private lateinit var electricityMeterValueProvider: ElectricityMeterValueProvider

  @MockK
  private lateinit var userStateHolder: UserStateHolder

  @InjectMockKs
  private lateinit var provider: ElectricityMeterValueStringProvider

  private val profileId = 1L
  private val remoteId = 100

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should delegate handle to electricity meter value provider`() {
    // given
    val channelWithChildren = channelWithChildren()
    every { electricityMeterValueProvider.handle(channelWithChildren) } returns true

    // when
    val result = provider.handle(channelWithChildren)

    // then
    assertThat(result).isTrue()
    verify { electricityMeterValueProvider.handle(channelWithChildren) }
  }

  @Test
  fun `should return aggregated value for list mode when settings use aggregation`() {
    // given
    val channelWithChildren = channelWithChildren(aggregatedValue = "15.20 kWh")
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR
    )

    // when
    val result = provider.value(channelWithChildren, ListFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo("15.20 kWh")
  }

  @Test
  fun `should return no value text for list mode when aggregation enabled and db value missing`() {
    // given
    val channelWithChildren = channelWithChildren(aggregatedValue = null)
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.CURRENT_DAY
    )

    // when
    val result = provider.value(channelWithChildren, ListFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo(NO_VALUE_TEXT)
  }

  @Test
  fun `should format non aggregated list voltage with custom unit`() {
    // given
    val channelWithChildren = channelWithChildren()
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.VOLTAGE
    )
    every { electricityMeterValueProvider.value(channelWithChildren, ListFirstValue) } returns 230.12

    // when
    val result = provider.value(channelWithChildren, ListFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo("230.1 V")
  }

  @Test
  fun `should suppress no value text for non aggregated list voltage`() {
    // given
    val channelWithChildren = channelWithChildren()
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.VOLTAGE
    )
    every { electricityMeterValueProvider.value(channelWithChildren, ListFirstValue) } returns 0.0

    // when
    val result = provider.value(channelWithChildren, ListFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo("0.00 V")
  }

  @Test
  fun `should show no value text for non aggregated forward active energy when provider returns zero`() {
    // given
    val channelWithChildren = channelWithChildren()
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY
    )
    every { electricityMeterValueProvider.value(channelWithChildren, ListFirstValue) } returns 0.0

    // when
    val result = provider.value(channelWithChildren, ListFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo(NO_VALUE_TEXT)
  }

  @Test
  fun `should format default value with default electricity meter unit`() {
    // given
    val channelWithChildren = channelWithChildren()
    every { electricityMeterValueProvider.value(channelWithChildren, DefaultFirstValue) } returns 12.34

    // when
    val result = provider.value(channelWithChildren, DefaultFirstValue, withUnit = true)

    // then
    assertThat(result).isEqualTo("12.34 kWh")
  }

  private fun settings(
    metricOnList: ElectricityMeterMeasurementType,
    metricOnListAggregation: ListValueAggregation = ListValueAggregation.NO_AGGREGATION
  ) = ElectricityMeterSettings(
    currentMonthBalancing = ElectricityMeterBalanceType.DEFAULT,
    metricOnList = metricOnList,
    metricOnListBalancing = ElectricityMeterBalanceType.ARITHMETIC,
    metricOnListAggregation = metricOnListAggregation
  )

  private fun channelWithChildren(aggregatedValue: String? = null): ChannelWithChildren {
    val channelValueEntity = io.mockk.mockk<ChannelValueEntity> {
      every { this@mockk.aggregatedValue } returns aggregatedValue
    }
    val channelData = io.mockk.mockk<ChannelDataEntity> {
      every { this@mockk.channelValueEntity } returns channelValueEntity
    }

    return io.mockk.mockk {
      every { this@mockk.profileId } returns this@ElectricityMeterValueStringProviderTest.profileId
      every { this@mockk.remoteId } returns this@ElectricityMeterValueStringProviderTest.remoteId
      every { this@mockk.channel } returns channelData
    }
  }
}
