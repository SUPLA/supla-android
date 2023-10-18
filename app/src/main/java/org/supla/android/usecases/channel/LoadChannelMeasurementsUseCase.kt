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
import org.supla.android.extensions.isThermometer
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) : BaseLoadMeasurementsUseCase() {

  operator fun invoke(remoteId: Int, startDate: Date, endDate: Date, aggregation: ChartDataAggregation): Maybe<List<HistoryDataSet>> =
    readChannelByRemoteIdUseCase(remoteId)
      .flatMapMerged { profileManager.getCurrentProfile() }
      .flatMap {
        if (it.first.isThermometer()) {
          buildDataSets(it.first, it.second.id, startDate, endDate, aggregation).firstElement()
        } else {
          Maybe.empty()
        }
      }

  private fun buildDataSets(
    channel: Channel,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    aggregation: ChartDataAggregation
  ): Observable<List<HistoryDataSet>> {
    val temperatureColors = TemperatureColors()
    val humidityColors = HumidityColors()
    return when (channel.func) {
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

      else -> Observable.empty()
    }
  }

  private fun humidityAndTemperatureObservable(
    channel: Channel,
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
}
