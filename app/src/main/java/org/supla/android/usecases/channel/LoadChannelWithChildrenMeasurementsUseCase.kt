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
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.supla.android.R
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.BaseTemperatureEntity
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.db.Channel
import org.supla.android.extensions.flatMapMerged
import org.supla.android.extensions.hasMeasurements
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.extensions.toTimestamp
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.history.HistoryDataSet
import org.supla.android.features.thermostatdetail.history.data.ChartDataAggregation
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_ALLOWED_DISTANCE_MULTIPLIER = 1.5f

// Server provides data for each 10 minutes
private const val AGGREGATING_MINUTES_DISTANCE_SEC = 600.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)

@Singleton
class LoadChannelWithChildrenMeasurementsUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) {

  operator fun invoke(remoteId: Int, startDate: Date, endDate: Date, aggregation: ChartDataAggregation): Maybe<List<HistoryDataSet>> =
    readChannelWithChildrenUseCase(remoteId)
      .flatMapMerged { profileManager.getCurrentProfile() }
      .flatMap {
        if (it.first.channel.isHvacThermostat()) {
          buildDataSets(it.first, it.second.id, startDate, endDate, aggregation).firstElement()
        } else {
          Maybe.empty()
        }
      }

  private fun buildDataSets(
    channelWithChildren: ChannelWithChildren,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    aggregation: ChartDataAggregation
  ): Observable<List<HistoryDataSet>> {
    val channelsWithMeasurements = mutableListOf<Channel>().also { list ->
      list.addAll(channelWithChildren.children.filter { it.channel.hasMeasurements() }.map { it.channel })
      if (channelWithChildren.channel.hasMeasurements()) {
        list.add(channelWithChildren.channel)
      }
    }

    val temperatureColors = TemperatureColors()
    val humidityColors = HumidityColors()
    val observables = mutableListOf<Observable<List<HistoryDataSet>>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          val color = temperatureColors.nextColor()
          list.add(
            temperatureLogRepository.findMeasurements(it.remoteId, profileId, startDate, endDate)
              .map { list -> aggregatingTemperature(list, aggregation) }
              .map { measurements -> listOf(historyDataSet(it, ChartEntryType.TEMPERATURE, color, aggregation, measurements)) }
          )
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          val firstColor = temperatureColors.nextColor()
          val secondColor = humidityColors.nextColor()
          list.add(
            temperatureAndHumidityLogRepository.findMeasurements(it.remoteId, profileId, startDate, endDate)
              .map { measurements ->
                listOf(
                  historyDataSet(
                    it,
                    ChartEntryType.TEMPERATURE,
                    firstColor,
                    aggregation,
                    aggregatingTemperature(measurements, aggregation)
                  ),
                  historyDataSet(it, ChartEntryType.HUMIDITY, secondColor, aggregation, aggregatingHumidity(measurements, aggregation))
                )
              }
          )
        }
      }
    }

    return Observable.zip(observables) { items ->
      mutableListOf<HistoryDataSet>().also { list ->
        items.forEach {
          (it as? List<*>)?.let { dataSets ->
            list.addAll(dataSets.filterIsInstance(HistoryDataSet::class.java))
          }
        }
      }
    }
  }

  private fun <T : BaseTemperatureEntity> aggregatingTemperature(
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
      .groupBy { item -> item.date.toTimestamp().div(aggregation.timeInSec) }
      .map { group ->
        AggregatedEntity(
          group.key.times(aggregation.timeInSec),
          group.value.map { it.temperature!! }.average().toFloat()
        )
      }
  }

  private fun aggregatingHumidity(
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
      .groupBy { item -> item.date.toTimestamp().div(aggregation.timeInSec) }
      .map { group ->
        AggregatedEntity(
          group.key.times(aggregation.timeInSec),
          group.value.map { it.humidity!! }.average().toFloat()
        )
      }
  }

  private fun historyDataSet(
    channel: Channel,
    type: ChartEntryType,
    color: Int,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ) =
    HistoryDataSet(
      setId = HistoryDataSet.Id(channel.remoteId, type),
      function = channel.func,
      iconProvider = { context -> ImageCache.getBitmap(context, channel.imageIdx) },
      valueProvider = { context -> context.valuesFormatter.getTemperatureString(channel.value.getTemp(channel.func)) },
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
        val entry = Entry(entity.date.toFloat(), entity.value, type)

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
}

private abstract class Colors(
  private val colors: List<Int>,
  private var position: Int = 0
) {
  fun nextColor(): Int =
    colors[position % colors.size].also {
      position++
    }
}

private class TemperatureColors : Colors(listOf(R.color.red, R.color.dark_red))
private class HumidityColors : Colors(listOf(R.color.blue, R.color.dark_blue))

private data class AggregatedEntity(
  val date: Long,
  val value: Float
)
