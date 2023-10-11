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
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.BaseTemperatureEntity
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.flatMapMerged
import org.supla.android.extensions.hasMeasurements
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.history.HistoryDataSet
import org.supla.android.features.thermostatdetail.history.data.ChartDataAggregation
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.views.HUMIDITY_SCALE_FACTOR
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

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

    val colors = Colors()
    val observables = mutableListOf<Observable<List<HistoryDataSet>>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          val color = colors.nextColor()
          list.add(
            temperatureLogRepository.findMeasurements(it.remoteId, profileId, startDate, endDate)
              .map { measurements -> listOf(historyDataSet(it, color, measurements).aggregating(aggregation)) }
          )
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          val firstColor = colors.nextColor()
          val secondColor = colors.nextColor()
          list.add(
            temperatureAndHumidityLogRepository.findMeasurements(it.remoteId, profileId, startDate, endDate)
              .map { measurements ->
                listOf(
                  historyDataSet(it, firstColor, measurements).aggregating(aggregation),
                  historyDataSetHumidity(it, secondColor, measurements).aggregating(aggregation)
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

  private fun <T : BaseTemperatureEntity> historyDataSet(channel: Channel, color: Int, measurements: List<T>) =
    HistoryDataSet(
      setId = HistoryDataSet.Id(channel.remoteId, HistoryDataSet.Type.TEMPERATURE),
      function = channel.func,
      iconProvider = { context -> ImageCache.getBitmap(context, channel.imageIdx) },
      valueProvider = { context -> context.valuesFormatter.getTemperatureString(channel.value.getTemp(channel.func)) },
      color = color,
      entries = measurements.filter { entity -> entity.temperature != null }.map { entity ->
        Entry(entity.date.time.div(1000).toFloat(), entity.temperature!!)
      }
    )

  private fun historyDataSetHumidity(channel: Channel, color: Int, measurements: List<TemperatureAndHumidityLogEntity>) =
    HistoryDataSet(
      setId = HistoryDataSet.Id(channel.remoteId, HistoryDataSet.Type.HUMIDITY),
      function = channel.func,
      iconProvider = { context -> ImageCache.getBitmap(context, channel.getImageIdx(ChannelBase.WhichOne.Second)) },
      valueProvider = { context -> context.valuesFormatter.getHumidityString(channel.value.humidity) },
      color = color,
      entries = measurements.filter { entity -> entity.humidity != null }.map { entity ->
        Entry(entity.date.time.div(1000).toFloat(), entity.humidity!!.div(HUMIDITY_SCALE_FACTOR))
      }
    )
}

private data class Colors(
  private val colors: List<Int> = listOf(R.color.primary, R.color.blue, R.color.red, R.color.violet),
  private var position: Int = 0
) {
  fun nextColor(): Int =
    colors[position % colors.size].also {
      position++
    }
}
