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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.icon.GetChannelIconUseCase
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class LoadChannelMeasurementsUseCaseTest : BaseLoadMeasurementsUseCaseTest() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @Mock
  private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @Mock
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var gson: Gson

  @InjectMocks
  private lateinit var useCase: LoadChannelMeasurementsUseCase

  @Test
  fun `should load temperature measurements`() {
    val entities = mockEntities(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureTest(entities, ChartDataAggregation.MINUTES) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          entities.map { entity ->
            tuple(
              entity.date.toTimestamp(),
              entity.temperature,
              null,
              null,
              null,
              null,
              ChartDataAggregation.MINUTES,
              ChartEntryType.TEMPERATURE
            )
          }
        )
    }
  }

  @Test
  fun `should load temperature measurements (hours aggregation)`() {
    val entities = mockEntities(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureTest(entities, ChartDataAggregation.HOURS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, 10, 11, 0, 30).toTimestamp(),
              2.5f,
              0f,
              5f,
              0f,
              5f,
              ChartDataAggregation.HOURS,
              ChartEntryType.TEMPERATURE
            ),
            tuple(
              date(2022, 10, 11, 1, 30).toTimestamp(),
              8f,
              6f,
              10f,
              6f,
              10f,
              ChartDataAggregation.HOURS,
              ChartEntryType.TEMPERATURE
            )
          )
        )
    }
  }

  @Test
  fun `should load temperature measurements (days aggregation)`() {
    val entities = mockEntities(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureTest(entities, ChartDataAggregation.DAYS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, 10, 11, 12).toTimestamp(),
              5f,
              0f,
              10f,
              0f,
              10f,
              ChartDataAggregation.DAYS,
              ChartEntryType.TEMPERATURE
            )
          )
        )
    }
  }

  @Test
  fun `should load temperature measurements (months aggregation)`() {
    val entities = mockEntities(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureTest(entities, ChartDataAggregation.MONTHS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, 10, 15).toTimestamp(),
              5f,
              0f,
              10f,
              0f,
              10f,
              ChartDataAggregation.MONTHS,
              ChartEntryType.TEMPERATURE
            )
          )
        )
    }
  }

  @Test
  fun `should load temperature measurements (year aggregation)`() {
    val entities = mockEntities(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureTest(entities, ChartDataAggregation.YEARS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, Calendar.JULY, 1).toTimestamp(),
              5f,
              0f,
              10f,
              0f,
              10f,
              ChartDataAggregation.YEARS,
              ChartEntryType.TEMPERATURE
            )
          )
        )
    }
  }

  @Test
  fun `should load temperature with humidity measurements`() {
    val entities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureWithHumidityTest(
      entities,
      ChartDataAggregation.MINUTES,
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            entities.map {
              tuple(it.date.toTimestamp(), it.temperature, null, null, null, null, ChartDataAggregation.MINUTES, ChartEntryType.TEMPERATURE)
            }
          )
      },
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            entities.map {
              tuple(it.date.toTimestamp(), it.humidity, null, null, null, null, ChartDataAggregation.MINUTES, ChartEntryType.HUMIDITY)
            }
          )
      }
    )
  }

  @Test
  fun `should load temperature with humidity measurements (hours aggregation)`() {
    val entities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureWithHumidityTest(
      entities,
      ChartDataAggregation.HOURS,
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 11, 0, 30).toTimestamp(),
                2.5f,
                0f,
                5f,
                0f,
                5f,
                ChartDataAggregation.HOURS,
                ChartEntryType.TEMPERATURE
              ),
              tuple(
                date(2022, 10, 11, 1, 30).toTimestamp(),
                8f,
                6f,
                10f,
                6f,
                10f,
                ChartDataAggregation.HOURS,
                ChartEntryType.TEMPERATURE
              )
            )
          )
      },
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 11, 0, 30).toTimestamp(),
                7.5f,
                5f,
                10f,
                10f,
                5f,
                ChartDataAggregation.HOURS,
                ChartEntryType.HUMIDITY
              ),
              tuple(
                date(2022, 10, 11, 1, 30).toTimestamp(),
                2.0f,
                0f,
                4f,
                4f,
                0f,
                ChartDataAggregation.HOURS,
                ChartEntryType.HUMIDITY
              )
            )
          )
      }
    )
  }

  @Test
  fun `should load temperature with humidity measurements (days aggregation)`() {
    val entities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureWithHumidityTest(
      entities,
      ChartDataAggregation.DAYS,
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 11, 12).toTimestamp(),
                5f,
                0f,
                10f,
                0f,
                10f,
                ChartDataAggregation.DAYS,
                ChartEntryType.TEMPERATURE
              )
            )
          )
      },
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 11, 12).toTimestamp(),
                5f,
                0f,
                10f,
                10f,
                0f,
                ChartDataAggregation.DAYS,
                ChartEntryType.HUMIDITY
              )
            )
          )
      }
    )
  }

  @Test
  fun `should load temperature with humidity measurements (months aggregation)`() {
    val entities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureWithHumidityTest(
      entities,
      ChartDataAggregation.MONTHS,
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 15).toTimestamp(),
                5f,
                0f,
                10f,
                0f,
                10f,
                ChartDataAggregation.MONTHS,
                ChartEntryType.TEMPERATURE
              )
            )
          )
      },
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, 10, 15).toTimestamp(),
                5f,
                0f,
                10f,
                10f,
                0f,
                ChartDataAggregation.MONTHS,
                ChartEntryType.HUMIDITY
              )
            )
          )
      }
    )
  }

  @Test
  fun `should load temperature with humidity measurements (year aggregation)`() {
    val entities = mockEntitiesWithHumidity(10, 10 * MINUTE_IN_MILLIS)

    doTemperatureWithHumidityTest(
      entities,
      ChartDataAggregation.YEARS,
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, Calendar.JULY, 1).toTimestamp(),
                5f,
                0f,
                10f,
                0f,
                10f,
                ChartDataAggregation.YEARS,
                ChartEntryType.TEMPERATURE
              )
            )
          )
      },
      { entries ->
        assertThat(entries)
          .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
          .containsExactlyElementsOf(
            listOf(
              tuple(
                date(2022, Calendar.JULY, 1).toTimestamp(),
                5f,
                0f,
                10f,
                10f,
                0f,
                ChartDataAggregation.YEARS,
                ChartEntryType.HUMIDITY
              )
            )
          )
      }
    )
  }

  @Test
  fun `should separate entries when there is a gab in dates`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val channel: ChannelDataEntity = mockk()
    val aggregation = ChartDataAggregation.MINUTES
    every { channel.channelEntity } returns mockk { every { function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

    val entities = mockEntities(10, 20 * MINUTE_IN_MILLIS)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureLogRepository.findMeasurements(remoteId, profileId, startDate, endDate))
      .thenReturn(Observable.just(entities))
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn("")

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, aggregation).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.TEMPERATURE), R.color.chart_temperature_1, true)
      )

    assertThat(result[0].entities)
      .extracting(
        { it[0].date },
        { it[0].value },
        { it[0].min },
        { it[0].max },
        { it[0].open },
        { it[0].close },
        { it[0].aggregation },
        { it[0].type }
      )
      .containsExactlyElementsOf(
        entities.map {
          tuple(it.date.toTimestamp(), it.temperature, null, null, null, null, aggregation, ChartEntryType.TEMPERATURE)
        }
      )

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(temperatureLogRepository).findMeasurements(remoteId, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, temperatureLogRepository)
    verifyZeroInteractions(temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should load general purpose meter`() {
    val entities = mockGpMeterEntities(10, 10 * MINUTE_IN_MILLIS)

    doGeneralPurposeMeterTest(entities, ChartDataAggregation.MINUTES) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          entities.map { entity ->
            tuple(
              entity.date.toTimestamp(),
              entity.value,
              null,
              null,
              null,
              null,
              ChartDataAggregation.MINUTES,
              ChartEntryType.GENERAL_PURPOSE_METER
            )
          }
        )
    }
  }

  @Test
  fun `should load general purpose meter (hours aggregation)`() {
    val entities = mockGpMeterEntities(10, 10 * MINUTE_IN_MILLIS)

    doGeneralPurposeMeterTest(entities, ChartDataAggregation.HOURS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, 10, 11, 0, 30).toTimestamp(),
              15f,
              null,
              null,
              null,
              null,
              ChartDataAggregation.HOURS,
              ChartEntryType.GENERAL_PURPOSE_METER
            ),
            tuple(
              date(2022, 10, 11, 1, 30).toTimestamp(),
              40f,
              null,
              null,
              null,
              null,
              ChartDataAggregation.HOURS,
              ChartEntryType.GENERAL_PURPOSE_METER
            )
          )
        )
    }
  }

  @Test
  fun `should load general purpose measurement`() {
    val entities = mockGpMeasurementEntities(10, 10 * MINUTE_IN_MILLIS)

    doGeneralPurposeMeasurementTest(entities, ChartDataAggregation.MINUTES) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          entities.map { entity ->
            tuple(
              entity.date.toTimestamp(),
              entity.valueAverage,
              entity.valueMin,
              entity.valueMax,
              entity.valueOpen,
              entity.valueClose,
              ChartDataAggregation.MINUTES,
              ChartEntryType.GENERAL_PURPOSE_MEASUREMENT
            )
          }
        )
    }
  }

  @Test
  fun `should load general purpose measurement (hours aggregation)`() {
    val entities = mockGpMeasurementEntities(10, 10 * MINUTE_IN_MILLIS)

    doGeneralPurposeMeasurementTest(entities, ChartDataAggregation.HOURS) { entries ->
      assertThat(entries)
        .extracting({ it.date }, { it.value }, { it.min }, { it.max }, { it.open }, { it.close }, { it.aggregation }, { it.type })
        .containsExactlyElementsOf(
          listOf(
            tuple(
              date(2022, 10, 11, 0, 30).toTimestamp(),
              2.5f,
              0f,
              10f,
              0f,
              10f,
              ChartDataAggregation.HOURS,
              ChartEntryType.GENERAL_PURPOSE_MEASUREMENT
            ),
            tuple(
              date(2022, 10, 11, 1, 30).toTimestamp(),
              8f,
              0f,
              10f,
              0f,
              10f,
              ChartDataAggregation.HOURS,
              ChartEntryType.GENERAL_PURPOSE_MEASUREMENT
            )
          )
        )
    }
  }

  private fun doTemperatureTest(
    entities: List<TemperatureLogEntity>,
    aggregation: ChartDataAggregation,
    entriesAssertion: (List<AggregatedEntity>) -> Unit
  ) {
    // given
    val remoteId = 123
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureLogRepository.findMeasurements(remoteId, profileId, startDate, endDate))
      .thenReturn(Observable.just(entities))
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn("")

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, aggregation).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.TEMPERATURE), R.color.chart_temperature_1, true)
      )

    entriesAssertion(result[0].entities[0])

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(temperatureLogRepository).findMeasurements(remoteId, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, temperatureLogRepository)
    verifyZeroInteractions(temperatureAndHumidityLogRepository)
  }

  private fun doTemperatureWithHumidityTest(
    entities: List<TemperatureAndHumidityLogEntity>,
    aggregation: ChartDataAggregation,
    temperatureEntriesAssertion: (List<AggregatedEntity>) -> Unit,
    humidityEntriesAssertion: (List<AggregatedEntity>) -> Unit
  ) {
    // given
    val remoteId = 123
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaConst.SUPLA_CHANNELFNC_THERMOMETER }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(temperatureAndHumidityLogRepository.findMeasurements(remoteId, profileId, startDate, endDate))
      .thenReturn(Observable.just(entities))
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn("")
    whenever(getChannelIconUseCase.getIconProvider(channel, IconType.SECOND)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel, ValueType.SECOND)).thenReturn("")

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, aggregation).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.TEMPERATURE), R.color.chart_temperature_1, true),
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.HUMIDITY), R.color.chart_humidity_1, true)
      )

    temperatureEntriesAssertion(result[0].entities[0])
    humidityEntriesAssertion(result[1].entities[0])

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(temperatureAndHumidityLogRepository).findMeasurements(remoteId, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, temperatureAndHumidityLogRepository)
    verifyZeroInteractions(temperatureLogRepository)
  }

  private fun doGeneralPurposeMeterTest(
    entities: List<GeneralPurposeMeterEntity>,
    aggregation: ChartDataAggregation,
    entriesAssertion: (List<AggregatedEntity>) -> Unit
  ) {
    // given
    val remoteId = 123
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
    every { channel.configEntity } returns null

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeterLogRepository.findMeasurements(remoteId, profileId, startDate, endDate))
      .thenReturn(Observable.just(entities))
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn("")

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, aggregation).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.GENERAL_PURPOSE_METER), R.color.chart_gpm, true)
      )

    entriesAssertion(result[0].entities[0])

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(generalPurposeMeterLogRepository).findMeasurements(remoteId, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, generalPurposeMeterLogRepository)
    verifyZeroInteractions(temperatureAndHumidityLogRepository, temperatureLogRepository)
  }

  private fun doGeneralPurposeMeasurementTest(
    entities: List<GeneralPurposeMeasurementEntity>,
    aggregation: ChartDataAggregation,
    entriesAssertion: (List<AggregatedEntity>) -> Unit
  ) {
    // given
    val remoteId = 123
    val profileId = 321L
    val startDate = date(2022, 10, 11)
    val endDate = date(2022, 11, 11)
    val channel: ChannelDataEntity = mockk()
    every { channel.channelEntity } returns mockk { every { function } returns SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT }
    every { channel.remoteId } returns remoteId
    every { channel.function } returns SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
    every { channel.configEntity } returns null

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channel))
    whenever(generalPurposeMeasurementLogRepository.findMeasurements(remoteId, profileId, startDate, endDate))
      .thenReturn(Observable.just(entities))
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn("")

    // when
    val testObserver = useCase.invoke(remoteId, profileId, startDate, endDate, aggregation).test()

    // then
    testObserver.assertComplete()
    testObserver.assertValueCount(1)

    val result = testObserver.values()[0]
    assertThat(result).extracting({ it.setId }, { it.color }, { it.active })
      .containsExactly(
        tuple(HistoryDataSet.Id(remoteId, ChartEntryType.GENERAL_PURPOSE_MEASUREMENT), R.color.chart_temperature_1, true)
      )

    entriesAssertion(result[0].entities[0])

    verify(readChannelByRemoteIdUseCase).invoke(remoteId)
    verify(generalPurposeMeasurementLogRepository).findMeasurements(remoteId, profileId, startDate, endDate)
    verifyNoMoreInteractions(readChannelByRemoteIdUseCase, generalPurposeMeasurementLogRepository)
    verifyZeroInteractions(temperatureAndHumidityLogRepository, temperatureLogRepository, generalPurposeMeterLogRepository)
  }
}
