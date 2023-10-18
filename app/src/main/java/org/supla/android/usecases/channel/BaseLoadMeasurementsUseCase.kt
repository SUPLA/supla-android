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

import com.github.mikephil.charting.data.Entry
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.local.entity.BaseTemperatureEntity
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.toTimestamp
import org.supla.android.extensions.valuesFormatter
import org.supla.android.images.ImageCache

abstract class BaseLoadMeasurementsUseCase {

  internal fun <T : BaseTemperatureEntity> aggregatingTemperature(
    measurements: List<T>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .filter { it.temperature != null }
        .map { AggregatedEntity(it.date.toTimestamp(), it.temperature!!) }
    }

    return measurements
      .filter { it.temperature != null }
      .groupBy { item -> aggregation.aggregator(item.date) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.temperature!! }.average().toFloat(),
          min = group.value.minOf { it.temperature!! },
          max = group.value.maxOf { it.temperature!! }
        )
      }
  }

  internal fun aggregatingHumidity(
    measurements: List<TemperatureAndHumidityLogEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .filter { it.temperature != null }
        .map { AggregatedEntity(it.date.toTimestamp(), it.humidity!!) }
    }

    return measurements
      .filter { it.humidity != null }
      .groupBy { item -> aggregation.aggregator(item.date) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = group.value.map { it.humidity!! }.average().toFloat(),
          min = group.value.minOf { it.humidity!! },
          max = group.value.maxOf { it.humidity!! }
        )
      }
  }

  internal fun historyDataSet(
    channel: Channel,
    type: ChartEntryType,
    color: Int,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ) =
    HistoryDataSet(
      setId = HistoryDataSet.Id(channel.remoteId, type),
      type = type,
      iconProvider = when (type) {
        ChartEntryType.TEMPERATURE -> { context -> ImageCache.getBitmap(context, channel.imageIdx) }
        ChartEntryType.HUMIDITY -> { context -> ImageCache.getBitmap(context, channel.getImageIdx(ChannelBase.WhichOne.Second)) }
      },
      valueProvider = when (type) {
        ChartEntryType.TEMPERATURE -> { context -> context.valuesFormatter.getTemperatureString(channel.value.getTemp(channel.func)) }
        ChartEntryType.HUMIDITY -> { context -> context.valuesFormatter.getHumidityString(channel.value.humidity) }
      },
      color = color,
      entries = divideSetToSubsets(
        entities = measurements,
        type = type,
        aggregation = aggregation
      )
    )

  private fun divideSetToSubsets(
    entities: List<AggregatedEntity>,
    aggregation: ChartDataAggregation,
    type: ChartEntryType
  ): List<List<Entry>> {
    return mutableListOf<List<Entry>>().also { list ->
      var set = mutableListOf<Entry>()
      for (entity in entities) {
        val entry = Entry(entity.date.toFloat(), entity.value, entity.toDetails(type))

        set.lastOrNull()?.let {
          val distance = if (aggregation == ChartDataAggregation.MINUTES) {
            AGGREGATING_MINUTES_DISTANCE_SEC
          } else {
            aggregation.timeInSec.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
          }

          if (entry.x - it.x > distance) {
            list.add(set)
            set = mutableListOf()
          }
        }

        set.add(entry)
      }

      if (set.isNotEmpty()) {
        list.add(set)
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

internal class TemperatureColors : Colors(listOf(R.color.red, R.color.dark_red))
internal class HumidityColors : Colors(listOf(R.color.blue, R.color.dark_blue))

internal data class AggregatedEntity(
  val date: Long,
  val value: Float,
  val min: Float? = null,
  val max: Float? = null
) {

  fun toDetails(type: ChartEntryType) =
    EntryDetails(type, min, max)
}

data class EntryDetails(
  val type: ChartEntryType,
  val min: Float? = null,
  val max: Float? = null
)
