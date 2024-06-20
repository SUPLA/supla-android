package org.supla.android.usecases.channel
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the``````
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.google.gson.Gson
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.measurements.BaseTemperatureEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.extensions.toTimestamp
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueformatter.HumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.icon.GetChannelIconUseCase

abstract class BaseLoadMeasurementsUseCase(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val preferences: Preferences,
  private val gson: Gson // GSON_FOR_REPO
) {

  internal fun <T : BaseTemperatureEntity> aggregatingTemperature(
    measurements: List<T>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .filter { it.temperature != null }
        .map { AggregatedEntity(ChartEntryType.TEMPERATURE, aggregation, it.date.toTimestamp(), it.temperature!!) }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .asSequence()
      .filter { it.temperature != null }
      .groupBy { item -> aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          type = ChartEntryType.TEMPERATURE,
          aggregation = aggregation,
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.temperature!! }.average().toFloat(),
          min = group.value.minOf { it.temperature!! },
          max = group.value.maxOf { it.temperature!! },
          open = group.value.firstOrNull()?.temperature,
          close = group.value.lastOrNull()?.temperature
        )
      }
      .toList()
  }

  internal fun aggregatingHumidity(
    measurements: List<TemperatureAndHumidityLogEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .filter { it.temperature != null }
        .map { AggregatedEntity(ChartEntryType.HUMIDITY, aggregation, it.date.toTimestamp(), it.humidity!!) }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .asSequence()
      .filter { it.humidity != null }
      .groupBy { item -> aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          type = ChartEntryType.HUMIDITY,
          aggregation = aggregation,
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.humidity!! }.average().toFloat(),
          min = group.value.minOf { it.humidity!! },
          max = group.value.maxOf { it.humidity!! },
          open = group.value.firstOrNull()?.humidity,
          close = group.value.lastOrNull()?.humidity
        )
      }
      .toList()
  }

  internal fun historyDataSet(
    channel: ChannelDataEntity,
    type: ChartEntryType,
    color: Int,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ) =
    HistoryDataSet(
      setId = HistoryDataSet.Id(channel.remoteId, type),
      iconProvider = when (type) {
        ChartEntryType.HUMIDITY -> getChannelIconUseCase.getIconProvider(channel, IconType.SECOND)
        else -> getChannelIconUseCase.getIconProvider(channel)
      },
      value = when (type) {
        ChartEntryType.HUMIDITY -> getChannelValueStringUseCase(channel, ValueType.SECOND)
        else -> getChannelValueStringUseCase(channel)
      },
      valueFormatter = getValueFormatter(type, channel),
      color = color,
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  private fun getValueFormatter(type: ChartEntryType, channel: ChannelDataEntity): ChannelValueFormatter {
    return when (type) {
      ChartEntryType.HUMIDITY -> HumidityValueFormatter()
      ChartEntryType.TEMPERATURE -> ThermometerValueFormatter(preferences)
      ChartEntryType.GENERAL_PURPOSE_MEASUREMENT,
      ChartEntryType.GENERAL_PURPOSE_METER ->
        GpmValueFormatter(channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelGeneralPurposeBaseConfig)
    }
  }

  private fun divideSetToSubsets(
    entities: List<AggregatedEntity>,
    aggregation: ChartDataAggregation
  ): List<List<AggregatedEntity>> {
    return mutableListOf<List<AggregatedEntity>>().also { list ->
      var sublist = mutableListOf<AggregatedEntity>()
      for (entity in entities) {
        sublist.lastOrNull()?.let {
          val distance = if (aggregation == ChartDataAggregation.MINUTES) {
            AGGREGATING_MINUTES_DISTANCE_SEC
          } else {
            aggregation.timeInSec.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
          }

          if (entity.date - it.date > distance) {
            list.add(sublist)
            sublist = mutableListOf()
          }
        }

        sublist.add(entity)
      }

      if (sublist.isNotEmpty()) {
        list.add(sublist)
      }
    }
  }

  companion object {
    internal const val MAX_ALLOWED_DISTANCE_MULTIPLIER = 1.5f

    // Server provides data for each 10 minutes
    internal const val AGGREGATING_MINUTES_DISTANCE_SEC = 600.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
  }
}

internal abstract class Colors(
  private val colors: List<Int>,
  private var position: Int = 0
) {
  fun nextColor(): Int =
    colors[position % colors.size].also {
      position++
    }
}

internal class TemperatureColors : Colors(listOf(R.color.chart_temperature_1, R.color.chart_temperature_2))
internal class HumidityColors : Colors(listOf(R.color.chart_humidity_1, R.color.chart_humidity_2))

data class AggregatedEntity(
  val type: ChartEntryType,
  val aggregation: ChartDataAggregation,
  val date: Long,
  val value: Float,
  val min: Float? = null,
  val max: Float? = null,
  val open: Float? = null,
  val close: Float? = null
)
