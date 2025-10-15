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
import org.supla.android.core.shared.provider
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.TemperatureChartColors
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TemperatureMeasurementsProvider @Inject constructor(
  private val temperatureLogRepository: TemperatureLogRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  preferences: ApplicationPreferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {
  override fun handle(channelWithChildren: ChannelWithChildren) =
    channelWithChildren.channel.function == SuplaFunction.THERMOMETER

  override fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    val entryType = ChartEntryType.TEMPERATURE
    val color = colorProvider?.let { it(entryType) } ?: TemperatureChartColors.DEFAULT
    val channel = channelWithChildren.channel

    return temperatureLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { entities -> aggregatingTemperature(entities, spec.aggregation) }
      .map { measurements -> listOf(historyDataSet(channelWithChildren, entryType, color, spec.aggregation, measurements)) }
      .map {
        ChannelChartSets(
          channel.remoteId,
          channel.function,
          getCaptionUseCase(channel.shareable).provider(),
          spec.aggregation,
          it
        )
      }
      .firstOrError()
  }
}
