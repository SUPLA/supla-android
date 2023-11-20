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
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.db.Channel
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

@RunWith(MockitoJUnitRunner::class)
class LoadChannelWithChildrenMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {
  @Mock
  private lateinit var readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @Mock
  private lateinit var getChannelValueUseCase: GetChannelValueUseCase

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
    val channel: Channel = mockk()
    every { channel.remoteId } returns remoteId
    every { channel.func } returns SUPLA_CHANNELFNC_THERMOMETER

    whenever(readChannelWithChildrenUseCase.invoke(remoteId)).thenReturn(Maybe.just(mockChannelWithChildren()))
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
        Assertions.tuple(HistoryDataSet.Id(2, ChartEntryType.TEMPERATURE), R.color.red, true),
        Assertions.tuple(HistoryDataSet.Id(2, ChartEntryType.HUMIDITY), R.color.blue, true),
        Assertions.tuple(HistoryDataSet.Id(3, ChartEntryType.TEMPERATURE), R.color.dark_red, true)
      )

    val temperatureEntryDetails = EntryDetails(ChartDataAggregation.MINUTES, ChartEntryType.TEMPERATURE, null, null)
    Assertions.assertThat(result[0].entries[0])
      .extracting({ it.x }, { it.y }, { it.data })
      .containsExactlyElementsOf(
        temperatureAndHumidityEntities.map {
          Assertions.tuple(
            it.date.toTimestamp().toFloat(),
            it.temperature,
            temperatureEntryDetails
          )
        }
      )
    val humidityEntryDetails = EntryDetails(ChartDataAggregation.MINUTES, ChartEntryType.HUMIDITY, null, null)
    Assertions.assertThat(result[1].entries[0])
      .extracting({ it.x }, { it.y }, { it.data })
      .containsExactlyElementsOf(
        temperatureAndHumidityEntities.map {
          Assertions.tuple(
            it.date.toTimestamp().toFloat(),
            it.humidity,
            humidityEntryDetails
          )
        }
      )
    Assertions.assertThat(result[2].entries[0])
      .extracting({ it.x }, { it.y }, { it.data })
      .containsExactlyElementsOf(
        temperatureEntities.map {
          Assertions.tuple(
            it.date.toTimestamp().toFloat(),
            it.temperature,
            temperatureEntryDetails
          )
        }
      )

    verify(readChannelWithChildrenUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMeasurements(2, profileId, startDate, endDate)
    verify(temperatureLogRepository).findMeasurements(3, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelWithChildrenUseCase, temperatureLogRepository, temperatureAndHumidityLogRepository)
  }

  private fun mockChannelWithChildren(): ChannelWithChildren =
    mockk<ChannelWithChildren>().also { channelWithChildren ->
      every { channelWithChildren.channel } returns mockk<Channel>().also {
        every { it.remoteId } returns 1
        every { it.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
      }
      every { channelWithChildren.children } returns listOf(
        mockChannelChild(ChannelRelationType.MAIN_THERMOMETER, 2, SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE),
        mockChannelChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 3, SUPLA_CHANNELFNC_THERMOMETER)
      )
    }

  private fun mockChannelChild(relation: ChannelRelationType, remoteId: Int, function: Int): ChannelChild =
    mockk<ChannelChild>().also { channelChild ->
      every { channelChild.relationType } returns relation
      every { channelChild.channel } returns mockk<Channel>().also {
        every { it.remoteId } returns remoteId
        every { it.func } returns function
      }
    }
}
