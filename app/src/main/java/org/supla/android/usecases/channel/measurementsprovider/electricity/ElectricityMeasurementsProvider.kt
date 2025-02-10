package org.supla.android.usecases.channel.measurementsprovider.electricity
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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.measurements.ElectricityBaseLogEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Measurement
import org.supla.android.usecases.channel.measurementsprovider.MeasurementsProvider
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.VoltageValueFormatter
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.extensions.ifTrue
import javax.inject.Named

open class ElectricityMeasurementsProvider<T : ElectricityBaseLogEntity>(
  private val getChannelIconUseCase: GetChannelIconUseCase,
  @Named(GSON_FOR_REPO) gson: Gson,
  preferences: Preferences
) : MeasurementsProvider(preferences, gson) {

  open val labelValueExtractor: (Measurement?) -> Double
    get() = { 0.0 }

  protected fun aggregating(
    measurements: List<T>,
    aggregation: ChartDataAggregation
  ): List<AggregatedEntity> {
    if (aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map {
          AggregatedEntity(
            date = it.date.toTimestamp(),
            value = AggregatedValue.WithPhase(
              value = it.avg,
              min = it.min,
              max = it.max,
              phase = it.phase
            )
          )
        }
    }

    return measurements
      .groupBy { item -> aggregation.aggregator(item) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          date = aggregation.groupTimeProvider(group.value.first().date),
          value = AggregatedValue.WithPhase(
            value = group.value.map { it.avg }.average().toFloat(),
            min = group.value.minOf { it.min },
            max = group.value.maxOf { it.max },
            phase = group.value.first().phase
          )
        )
      }
      .toList()
  }

  protected fun historyDataSet(
    channel: ChannelDataEntity,
    phase: Phase,
    isFirst: Boolean,
    type: ChartEntryType,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = type,
      label = createLabel(channel, phase, isFirst),
      valueFormatter = getValueFormatter(type, channel),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  private fun createLabel(channel: ChannelDataEntity, phase: Phase, isFirst: Boolean): HistoryDataSet.Label {
    val icon = isFirst.ifTrue { getChannelIconUseCase(channel) }
    val electricity = channel.Electricity
    val phases = electricity.phases

    if (phases.contains(phase)) {
      electricity.value?.let {
        val value = VoltageValueFormatter.format(
          value = labelValueExtractor(it.getMeasurement(phase.value, 0)),
          precision = ChannelValueFormatter.Custom(value = 1),
          withUnit = false
        )
        return HistoryDataSet.Label.Single(HistoryDataSet.LabelData(icon, value, phase.color))
      }
    }

    return HistoryDataSet.Label.Single(HistoryDataSet.LabelData(icon, ValuesFormatter.NO_VALUE_TEXT, R.color.disabled))
  }
}
