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
import org.supla.android.R
import org.supla.android.core.shared.provider
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartEntryType.GENERAL_PURPOSE_METER
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GeneralPurposeMeterMeasurementsProvider @Inject constructor(
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(channelWithChildren: ChannelWithChildren) =
    channelWithChildren.channel.function == SuplaFunction.GENERAL_PURPOSE_METER

  override fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    val channel = channelWithChildren.channel

    return generalPurposeMeterLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { entities -> aggregatingGeneralCounter(entities, spec.aggregation) }
      .map { measurements ->
        listOf(historyDataSet(channelWithChildren, GENERAL_PURPOSE_METER, R.color.chart_gpm, spec.aggregation, measurements))
      }
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

  private fun aggregatingGeneralCounter(
    measurements: List<GeneralPurposeMeterEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            date = it.date.toTimestamp(),
            value = AggregatedValue.Single(it.valueIncrement)
          )
        }
    }

    return measurements
      .groupBy { item -> aggregation.aggregator(item) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = AggregatedValue.Single(group.value.map { it.valueIncrement }.sum())
        )
      }
      .toList()
  }
}
