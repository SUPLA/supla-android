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
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartEntryType.GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.data.model.chart.ChartEntryType.GENERAL_PURPOSE_METER
import org.supla.android.data.model.chart.TemperatureChartColors
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GeneralPurposeMeasurementMeasurementsProvider @Inject constructor(
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {
  override fun handle(function: Int) = function == SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

  override fun provide(
    channel: ChannelDataEntity,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> {
    val color = colorProvider?.let { it(GENERAL_PURPOSE_METER) } ?: TemperatureChartColors.DEFAULT

    return generalPurposeMeasurementLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { entities -> aggregatingGeneralMeasurement(entities, spec.aggregation) }
      .map { measurements -> listOf(historyDataSet(channel, GENERAL_PURPOSE_MEASUREMENT, color, spec.aggregation, measurements)) }
      .map {
        ChannelChartSets(
          channel.remoteId,
          channel.function,
          getChannelCaptionUseCase(channel.channelEntity),
          spec.aggregation,
          it
        )
      }
      .firstOrError()
  }

  private fun aggregatingGeneralMeasurement(
    measurements: List<GeneralPurposeMeasurementEntity>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            date = it.date.toTimestamp(),
            value = AggregatedValue.Single(
              value = it.valueAverage,
              min = it.valueMin,
              max = it.valueMax,
              open = it.valueOpen,
              close = it.valueClose
            )
          )
        }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .groupBy { item -> aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          value = AggregatedValue.Single(
            value = group.value.map { it.valueAverage }.average().toFloat(),
            min = group.value.minOf { it.valueMin },
            max = group.value.maxOf { it.valueMax },
            open = group.value.firstOrNull()?.valueOpen,
            close = group.value.lastOrNull()?.valueClose
          )
        )
      }
      .toList()
  }
}
