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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartEntryType.GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.data.model.chart.ChartEntryType.GENERAL_PURPOSE_METER
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isGpm
import org.supla.android.data.source.local.entity.complex.isThermometer
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.icon.GetChannelIconUseCase
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsUseCase @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : BaseLoadMeasurementsUseCase(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  operator fun invoke(
    remoteId: Int,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    aggregation: ChartDataAggregation
  ): Single<List<HistoryDataSet>> =
    readChannelByRemoteIdUseCase(remoteId)
      .toSingle()
      .flatMap {
        if (it.isThermometer() || it.isGpm()) {
          buildDataSets(it, profileId, startDate, endDate, aggregation).firstOrError()
        } else {
          Single.error(IllegalArgumentException("Channel function not supported (${it.function}"))
        }
      }

  private fun buildDataSets(
    channel: ChannelDataEntity,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    aggregation: ChartDataAggregation
  ): Observable<List<HistoryDataSet>> {
    val temperatureColors = TemperatureColors()
    val humidityColors = HumidityColors()
    return when (channel.function) {
      SuplaConst.SUPLA_CHANNELFNC_THERMOMETER -> {
        val color = temperatureColors.nextColor()
        temperatureLogRepository.findMeasurements(channel.remoteId, profileId, startDate, endDate)
          .map { entities -> aggregatingTemperature(entities, aggregation) }
          .map { measurements -> listOf(historyDataSet(channel, ChartEntryType.TEMPERATURE, color, aggregation, measurements)) }
      }

      SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE -> {
        val firstColor = temperatureColors.nextColor()
        val secondColor = humidityColors.nextColor()
        humidityAndTemperatureObservable(channel, profileId, startDate, endDate, aggregation, firstColor, secondColor)
      }

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT -> {
        val color = temperatureColors.nextColor()
        generalPurposeMeasurementLogRepository.findMeasurements(channel.remoteId, profileId, startDate, endDate)
          .map { entities -> aggregatingGeneralMeasurement(entities, aggregation) }
          .map { measurements -> listOf(historyDataSet(channel, GENERAL_PURPOSE_MEASUREMENT, color, aggregation, measurements)) }
      }

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER -> {
        generalPurposeMeterLogRepository.findMeasurements(channel.remoteId, profileId, startDate, endDate)
          .map { entities -> aggregatingGeneralCounter(entities, aggregation) }
          .map { measurements -> listOf(historyDataSet(channel, GENERAL_PURPOSE_METER, R.color.chart_gpm, aggregation, measurements)) }
      }

      else -> Observable.empty()
    }
  }

  private fun humidityAndTemperatureObservable(
    channel: ChannelDataEntity,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    aggregation: ChartDataAggregation,
    firstColor: Int,
    secondColor: Int
  ) =
    temperatureAndHumidityLogRepository.findMeasurements(channel.remoteId, profileId, startDate, endDate)
      .map { measurements ->
        listOf(
          historyDataSet(channel, ChartEntryType.TEMPERATURE, firstColor, aggregation, aggregatingTemperature(measurements, aggregation)),
          historyDataSet(channel, ChartEntryType.HUMIDITY, secondColor, aggregation, aggregatingHumidity(measurements, aggregation))
        )
      }

  private fun aggregatingGeneralMeasurement(
    measurements: List<GeneralPurposeMeasurementEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            type = GENERAL_PURPOSE_MEASUREMENT,
            aggregation = aggregation,
            date = it.date.toTimestamp(),
            value = it.valueAverage,
            min = it.valueMin,
            max = it.valueMax,
            open = it.valueOpen,
            close = it.valueClose
          )
        }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .groupBy { item -> aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          type = GENERAL_PURPOSE_MEASUREMENT,
          aggregation = aggregation,
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.valueAverage }.average().toFloat(),
          min = group.value.minOf { it.valueMin },
          max = group.value.maxOf { it.valueMax },
          open = group.value.firstOrNull()?.valueOpen,
          close = group.value.lastOrNull()?.valueClose
        )
      }
      .toList()
  }

  private fun aggregatingGeneralCounter(
    measurements: List<GeneralPurposeMeterEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            type = GENERAL_PURPOSE_METER,
            aggregation = aggregation,
            date = it.date.toTimestamp(),
            value = it.valueIncrement
          )
        }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .groupBy { item -> aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          type = GENERAL_PURPOSE_METER,
          aggregation = aggregation,
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.valueIncrement }.sum()
        )
      }
      .toList()
  }
}
