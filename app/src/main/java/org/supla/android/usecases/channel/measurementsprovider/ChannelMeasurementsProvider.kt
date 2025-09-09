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
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.singleLabel
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.complex.isImpulseCounter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.measurementsprovider.ChannelMeasurementsProvider.Companion.AGGREGATING_MINUTES_DISTANCE_SEC
import org.supla.android.usecases.channel.measurementsprovider.ChannelMeasurementsProvider.Companion.MAX_ALLOWED_DISTANCE_MULTIPLIER
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.ChartAxisElectricityMeterValueFormatter
import org.supla.android.usecases.channel.valueformatter.CurrentValueFormatter
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueformatter.HumidityValueFormatter
import org.supla.android.usecases.channel.valueformatter.ImpulseCounterChartValueFormatter
import org.supla.android.usecases.channel.valueformatter.PowerActiveValueFormatter
import org.supla.android.usecases.channel.valueformatter.ThermometerValueFormatter
import org.supla.android.usecases.channel.valueformatter.VoltageValueFormatter
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType

abstract class ChannelMeasurementsProvider(
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  preferences: ApplicationPreferences,
  gson: Gson // GSON_FOR_REPO
) : MeasurementsProvider(preferences, gson) {

  abstract fun handle(channelWithChildren: ChannelWithChildren): Boolean

  abstract fun provide(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)? = null
  ): Single<ChannelChartSets>

  protected fun historyDataSet(
    channelWithChildren: ChannelWithChildren,
    type: ChartEntryType,
    color: Int,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = type,
      label = singleLabel(
        imageId = when (type) {
          ChartEntryType.HUMIDITY -> getChannelIconUseCase(channelWithChildren.channel, IconType.SECOND)
          else -> getChannelIconUseCase(channelWithChildren.channel)
        },
        value = when (type) {
          ChartEntryType.HUMIDITY -> getChannelValueStringUseCase(channelWithChildren, ValueType.SECOND)
          else -> getChannelValueStringUseCase(channelWithChildren)
        },
        color = color,
      ),
      valueFormatter = getValueFormatter(type, channelWithChildren),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  companion object {
    internal const val MAX_ALLOWED_DISTANCE_MULTIPLIER = 1.5f

    // Server provides data for each 10 minutes
    internal const val AGGREGATING_MINUTES_DISTANCE_SEC = 600.times(MAX_ALLOWED_DISTANCE_MULTIPLIER)
  }
}

open class MeasurementsProvider(
  private val preferences: ApplicationPreferences,
  private val gson: Gson // GSON_FOR_REPO
) {

  protected fun getValueFormatter(type: ChartEntryType, channelWithChildren: ChannelWithChildren): ChannelValueFormatter {
    return when (type) {
      ChartEntryType.HUMIDITY,
      ChartEntryType.HUMIDITY_ONLY -> HumidityValueFormatter()

      ChartEntryType.TEMPERATURE -> ThermometerValueFormatter(preferences)
      ChartEntryType.GENERAL_PURPOSE_MEASUREMENT,
      ChartEntryType.GENERAL_PURPOSE_METER ->
        GpmValueFormatter(channelWithChildren.channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelGeneralPurposeBaseConfig)

      ChartEntryType.ELECTRICITY -> ChartAxisElectricityMeterValueFormatter()
      ChartEntryType.IMPULSE_COUNTER -> ImpulseCounterChartValueFormatter(unit = getImpulseCounterUnit(channelWithChildren))
      ChartEntryType.VOLTAGE -> VoltageValueFormatter
      ChartEntryType.CURRENT -> CurrentValueFormatter
      ChartEntryType.POWER_ACTIVE -> PowerActiveValueFormatter
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

  private fun getImpulseCounterUnit(channelWithChildren: ChannelWithChildren): String? =
    if (channelWithChildren.channel.isImpulseCounter()) {
      channelWithChildren.channel.channelExtendedValueEntity?.getSuplaValue()?.ImpulseCounterValue?.unit
    } else {
      channelWithChildren.children
        .firstOrNull { it.relationType == ChannelRelationType.METER }
        ?.channelDataEntity?.channelExtendedValueEntity?.getSuplaValue()?.ImpulseCounterValue?.unit
    }
}
