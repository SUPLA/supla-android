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
import org.supla.android.data.model.chart.ChartEntryType.IMPULSE_COUNTER
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.local.entity.complex.ImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.ui.views.charts.marker.ImpulseCounterCustomData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ImpulseCounterMeasurementsProvider @Inject constructor(
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(channelWithChildren: ChannelWithChildren) = channelWithChildren.isOrHasImpulseCounter

  override fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    val channel = channelWithChildren.channel

    return impulseCounterLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { entities -> aggregating(entities, spec.aggregation) }
      .map { result -> toHistoryDataSet(channelWithChildren, result, spec) }
      .map {
        ChannelChartSets(
          channel,
          getCaptionUseCase(channel.shareable).provider(),
          spec.aggregation,
          it,
          ImpulseCounterCustomData(
            channel.ImpulseCounter.value?.unit,
            channel.ImpulseCounter.value?.pricePerUnit?.toFloat(),
            channel.ImpulseCounter.value?.currency
          )
        )
      }
      .firstOrError()
  }

  private fun aggregating(
    measurements: List<ImpulseCounterLogEntity>,
    aggregation: ChartDataAggregation
  ): AggregationResult =
    AggregationResult(
      list = aggregatingImpulseCounter(measurements, aggregation),
      sum = listOf(measurements.map { it.calculatedValue }.sum())
    )

  private fun aggregatingImpulseCounter(
    measurements: List<ImpulseCounterLogEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            date = it.date.toTimestamp(),
            value = AggregatedValue.Single(it.calculatedValue)
          )
        }
    }

    return measurements
      .groupBy { item -> aggregation.aggregator(item) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = if (aggregation.isRank) group.key else aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = AggregatedValue.Single(group.value.map { it.calculatedValue }.sum())
        )
      }
      .toList()
      .let { list ->
        if (aggregation.isRank) {
          list.sortedByDescending { it.value.valueMax }
        } else {
          list
        }
      }
  }

  private fun toHistoryDataSet(
    channelWithChildren: ChannelWithChildren,
    result: AggregationResult,
    spec: ChartDataSpec
  ) =
    listOf(
      historyDataSet(
        channelWithChildren = channelWithChildren,
        label = HistoryDataSet.Label.Single(
          HistoryDataSet.LabelData(
            imageId = getChannelIconUseCase(channelWithChildren.channel),
            value = getValueFormatter(IMPULSE_COUNTER, channelWithChildren.channel).format(result.nextSum()),
            color = R.color.chart_gpm
          )
        ),
        aggregation = spec.aggregation,
        measurements = result.list
      )
    )

  private fun historyDataSet(
    channelWithChildren: ChannelWithChildren,
    label: HistoryDataSet.Label,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = IMPULSE_COUNTER,
      label = label,
      valueFormatter = getValueFormatter(ChartEntryType.ELECTRICITY, channelWithChildren.channel),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )
}
