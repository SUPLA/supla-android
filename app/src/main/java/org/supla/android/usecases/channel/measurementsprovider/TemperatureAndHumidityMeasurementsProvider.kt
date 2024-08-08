package org.supla.android.usecases.channel.measurementsprovider
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
import io.reactivex.rxjava3.core.Single
import org.supla.android.Preferences
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HumidityChartColors
import org.supla.android.data.model.chart.TemperatureChartColors
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.suplaFunction
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TemperatureAndHumidityMeasurementsProvider @Inject constructor(
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(function: Int) = function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

  override fun provide(
    channel: ChannelDataEntity,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> =
    temperatureAndHumidityLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { measurements ->
        listOf(
          historyDataSet(
            channel = channel,
            type = ChartEntryType.TEMPERATURE,
            color = colorProvider?.let { it(ChartEntryType.TEMPERATURE) } ?: TemperatureChartColors.DEFAULT,
            aggregation = spec.aggregation,
            measurements = aggregatingTemperature(measurements, spec.aggregation)
          ),
          historyDataSet(
            channel = channel,
            type = ChartEntryType.HUMIDITY,
            color = colorProvider?.let { it(ChartEntryType.HUMIDITY) } ?: HumidityChartColors.DEFAULT,
            aggregation = spec.aggregation,
            measurements = aggregatingHumidity(measurements, spec.aggregation)
          )
        )
      }
      .map {
        ChannelChartSets(
          channel.remoteId,
          channel.function.suplaFunction(),
          getChannelCaptionUseCase(channel.channelEntity),
          spec.aggregation,
          it
        )
      }
      .firstOrError()
}
