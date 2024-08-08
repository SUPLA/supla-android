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
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.singleLabel
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.ChartElectricityMeterValueFormatter
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueformatter.HumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.icon.GetChannelIconUseCase

abstract class ChannelMeasurementsProvider(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val preferences: Preferences,
  private val gson: Gson // GSON_FOR_REPO
) {

  abstract fun handle(function: Int): Boolean
  abstract fun provide(
    channel: ChannelDataEntity,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)? = null
  ): Single<ChannelChartSets>

  protected fun historyDataSet(
    channel: ChannelDataEntity,
    type: ChartEntryType,
    color: Int,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = type,
      label = singleLabel(
        iconProvider = when (type) {
          ChartEntryType.HUMIDITY -> getChannelIconUseCase.getIconProvider(channel, IconType.SECOND)
          else -> getChannelIconUseCase.getIconProvider(channel)
        },
        value = when (type) {
          ChartEntryType.HUMIDITY -> getChannelValueStringUseCase(channel, ValueType.SECOND)
          else -> getChannelValueStringUseCase(channel)
        },
        color = color,
      ),
      valueFormatter = getValueFormatter(type, channel),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  protected fun getValueFormatter(type: ChartEntryType, channel: ChannelDataEntity): ChannelValueFormatter {
    return when (type) {
      ChartEntryType.HUMIDITY -> HumidityValueFormatter()
      ChartEntryType.TEMPERATURE -> ThermometerValueFormatter(preferences)
      ChartEntryType.GENERAL_PURPOSE_MEASUREMENT,
      ChartEntryType.GENERAL_PURPOSE_METER ->
        GpmValueFormatter(channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelGeneralPurposeBaseConfig)

      ChartEntryType.ELECTRICITY -> ChartElectricityMeterValueFormatter()
    }
  }

  protected fun divideSetToSubsets(
    entities: List<AggregatedEntity>,
    aggregation: ChartDataAggregation
  ): List<List<AggregatedEntity>> {
    return mutableListOf<List<AggregatedEntity>>().also { list ->
      var sublist = mutableListOf<AggregatedEntity>()
      for (entity in entities) {
        sublist.lastOrNull()?.let {
          val distance = if (aggregation == ChartDataAggregation.MINUTES) {
            AGGREGATING_MINUTES_DISTANCE_SEC
          } else {
            aggregation.timeInSec.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
          }

          if (entity.date - it.date > distance) {
            list.add(sublist)
            sublist = mutableListOf()
          }
        }

        sublist.add(entity)
      }

      if (sublist.isNotEmpty()) {
        list.add(sublist)
      }
    }
  }

  companion object {
    internal const val MAX_ALLOWED_DISTANCE_MULTIPLIER = 1.5f

    // Server provides data for each 10 minutes
    internal const val AGGREGATING_MINUTES_DISTANCE_SEC = 600.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
  }
}
