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

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.icon.GetChannelIconUseCase

@RunWith(MockitoJUnitRunner::class)
class LoadChannelWithChildrenMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {
  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var gson: Gson

  @InjectMocks
  private lateinit var useCase: LoadChannelWithChildrenMeasurementsUseCase

  @Test
  fun `should load temperature measurements`() {
    // given
    val remoteId = 1
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val temperatureEntities = mockEntities(10, 10 * MINUTE_IN_MILLIS)
    val temperatureAndHumidityEntities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    val channelWithChildren = mockChannelWithChildren()
    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelWithChildren))
    whenever(temperatureAndHumidityLogRepository.findMeasurements(2, profileId, startDate, endDate))
      .thenReturn(Observable.just(temperatureAndHumidityEntities))
    whenever(temperatureLogRepository.findMeasurements(3, profileId, startDate, endDate))
      .thenReturn(Observable.just(temperatureEntities))

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, ChartDataAggregation.MINUTES).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    Assertions.assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(2, ChartEntryType.TEMPERATURE), R.color.chart_temperature_1, true),
        tuple(HistoryDataSet.Id(2, ChartEntryType.HUMIDITY), R.color.chart_humidity_1, true),
        tuple(HistoryDataSet.Id(3, ChartEntryType.TEMPERATURE), R.color.chart_temperature_2, true)
      )

    Assertions.assertThat(result[0].entities[0])
      .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
      .containsExactlyElementsOf(
        temperatureAndHumidityEntities.map {
          tuple(it.date.toTimestamp(), it.temperature, null, null, null, null, ChartDataAggregation.MINUTES, ChartEntryType.TEMPERATURE)
        }
      )
    Assertions.assertThat(result[1].entities[0])
      .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
      .containsExactlyElementsOf(
        temperatureAndHumidityEntities.map {
          tuple(it.date.toTimestamp(), it.humidity, null, null, null, null, ChartDataAggregation.MINUTES, ChartEntryType.HUMIDITY)
        }
      )
    Assertions.assertThat(result[2].entities[0])
      .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
      .containsExactlyElementsOf(
        temperatureEntities.map {
          tuple(it.date.toTimestamp(), it.temperature, null, null, null, null, ChartDataAggregation.MINUTES, ChartEntryType.TEMPERATURE)
        }
      )

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMeasurements(2, profileId, startDate, endDate)
    verify(temperatureLogRepository).findMeasurements(3, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureLogRepository, temperatureAndHumidityLogRepository)
  }

  private fun mockChannelWithChildren(): ChannelWithChildren =
    mockk<ChannelWithChildren>().also { channelWithChildren ->
      every { channelWithChildren.channel } returns mockk<ChannelDataEntity>().also {
        every { it.channelEntity } returns mockk { every { function } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT }
        every { it.remoteId } returns 1
        every { it.function } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
      }
      val child1 = mockChannelChild(ChannelRelationType.MAIN_THERMOMETER, 2, SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE)
      val child2 = mockChannelChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 3, SUPLA_CHANNELFNC_THERMOMETER)
      every { channelWithChildren.children } returns listOf(child1, child2)

      whenever(getChannelIconUseCase.getIconProvider(child1.channelDataEntity)).thenReturn { null }
      whenever(getChannelIconUseCase.getIconProvider(child1.channelDataEntity, IconType.SECOND)).thenReturn { null }
      whenever(getChannelIconUseCase.getIconProvider(child2.channelDataEntity)).thenReturn { null }
      whenever(getChannelValueStringUseCase.invoke(child1.channelDataEntity)).thenReturn("")
      whenever(getChannelValueStringUseCase.invoke(child1.channelDataEntity, ValueType.SECOND)).thenReturn("")
      whenever(getChannelValueStringUseCase.invoke(child2.channelDataEntity)).thenReturn("")
    }

  private fun mockChannelChild(relation: ChannelRelationType, remoteId: Int, function: Int): ChannelChildEntity =
    mockk<ChannelChildEntity>().also { channelChild ->
      every { channelChild.relationType } returns relation
      every { channelChild.channel } returns mockk { every { this@mockk.function } returns function }
      every { channelChild.channelDataEntity } returns mockk<ChannelDataEntity>().also {
        every { it.remoteId } returns remoteId
        every { it.function } returns function
      }
    }
}
