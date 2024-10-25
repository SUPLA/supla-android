package org.supla.android.usecases.channel
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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.date
import org.supla.android.usecases.channel.measurementsprovider.TemperatureAndHumidityMeasurementsProvider
import org.supla.android.usecases.channel.measurementsprovider.TemperatureMeasurementsProvider
import org.supla.core.shared.data.SuplaChannelFunction

@RunWith(MockitoJUnitRunner::class)
class LoadChannelWithChildrenMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {
  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var temperatureMeasurementsProvider: TemperatureMeasurementsProvider

  @Mock
  private lateinit var temperatureAndHumidityMeasurementsProvider: TemperatureAndHumidityMeasurementsProvider

  @InjectMocks
  private lateinit var useCase: LoadChannelWithChildrenMeasurementsUseCase

  @Test
  fun `should load temperature measurements`() {
    // given
    val remoteId = 1
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val spec = ChartDataSpec(startDate, endDate, ChartDataAggregation.MINUTES)
    val temperatureSets: ChannelChartSets = mockk()
    val temperatureAndHumiditySets: ChannelChartSets = mockk()

    val channelWithChildren = mockChannelWithChildren()
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureMeasurementsProvider.provide(eq(channelWithChildren.children[1].channelDataEntity), eq(spec), any()))
      .thenReturn(Single.just(temperatureSets))
    whenever(temperatureAndHumidityMeasurementsProvider.provide(eq(channelWithChildren.children[0].channelDataEntity), eq(spec), any()))
      .thenReturn(Single.just(temperatureAndHumiditySets))

    // when
    val testObserver = useCase.invoke(remoteId, ChartDataSpec(startDate, endDate, ChartDataAggregation.MINUTES)).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    Assertions.assertThat(result)
      .containsExactlyInAnyOrder(temperatureSets, temperatureAndHumiditySets)

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureMeasurementsProvider).provide(eq(channelWithChildren.children[1].channelDataEntity), eq(spec), any())
    verify(temperatureAndHumidityMeasurementsProvider).provide(eq(channelWithChildren.children[0].channelDataEntity), eq(spec), any())
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureMeasurementsProvider, temperatureAndHumidityMeasurementsProvider)
  }

  private fun mockChannelWithChildren(): ChannelWithChildren =
    mockk<ChannelWithChildren>().also { channelWithChildren ->
      every { channelWithChildren.channel } returns mockk<ChannelDataEntity>().also {
        every { it.channelEntity } returns mockk { every { function } returns SuplaChannelFunction.HVAC_THERMOSTAT }
        every { it.remoteId } returns 1
        every { it.function } returns SuplaChannelFunction.HVAC_THERMOSTAT
      }
      val child1 = mockChannelChild(ChannelRelationType.MAIN_THERMOMETER, 2, SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE)
      val child2 = mockChannelChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 3, SuplaChannelFunction.THERMOMETER)
      every { channelWithChildren.children } returns listOf(child1, child2)
    }

  private fun mockChannelChild(relation: ChannelRelationType, remoteId: Int, function: SuplaChannelFunction): ChannelChildEntity =
    mockk<ChannelChildEntity>().also { channelChild ->
      every { channelChild.relationType } returns relation
      every { channelChild.channel } returns mockk { every { this@mockk.function } returns function }
      every { channelChild.channelDataEntity } returns mockk<ChannelDataEntity>().also {
        every { it.remoteId } returns remoteId
        every { it.function } returns function
      }
    }
}
