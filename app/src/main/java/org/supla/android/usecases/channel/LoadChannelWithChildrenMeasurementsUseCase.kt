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

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.db.Channel
import org.supla.android.extensions.flatMapMerged
import org.supla.android.extensions.hasMeasurements
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelWithChildrenMeasurementsUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) : BaseLoadMeasurementsUseCase() {

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
}
