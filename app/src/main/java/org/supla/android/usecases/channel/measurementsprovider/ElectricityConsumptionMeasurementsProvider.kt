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
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.core.ui.fromResource
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.measurements.BalancedValue
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.balanceHourly
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.lib.SuplaConst
import org.supla.android.ui.views.charts.ElectricityMarkerCustomData
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters
import org.supla.android.usecases.channel.measurementsprovider.electricity.balanceValues
import org.supla.android.usecases.channel.measurementsprovider.electricity.chartBalancedValues
import org.supla.android.usecases.channel.measurementsprovider.electricity.getValues
import org.supla.android.usecases.channel.measurementsprovider.electricity.ifPhase1
import org.supla.android.usecases.channel.measurementsprovider.electricity.ifPhase2
import org.supla.android.usecases.channel.measurementsprovider.electricity.ifPhase3
import org.supla.android.usecases.channel.valueformatter.ChartMarkerElectricityMeterValueFormatter
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ElectricityConsumptionMeasurementsProvider @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  getChannelValueStringUseCase: GetChannelValueStringUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(function: Int) =
    when (function) {
      SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER,
      SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH -> true

      else -> false
    }

  override fun provide(
    channel: ChannelDataEntity,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelChartSets> =
    electricityMeterLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { aggregating(it, spec) }
      .map { listOf(historyDataSet(channel, labels(spec, getChannelIconUseCase.getIconProvider(channel), it), spec.aggregation, it.list)) }
      .map { historyDataSets ->
        ChannelChartSets(
          channel,
          getChannelCaptionUseCase(channel.channelEntity),
          spec.aggregation,
          historyDataSets,
          ElectricityMarkerCustomData(
            spec.customFilters as? ElectricityChartFilters,
            channel.Electricity.value?.pricePerUnit?.toFloat(),
            channel.Electricity.value?.currency
          )
        ) { context -> (spec.customFilters as? ElectricityChartFilters)?.type?.labelRes?.let { "${context.getString(it)} [kWh]" } ?: "" }
      }
      .firstOrError()

  private fun labels(spec: ChartDataSpec, icon: BitmapProvider, result: AggregationResult): HistoryDataSet.Label {
    val formatter = ChartMarkerElectricityMeterValueFormatter()

    return when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_VECTOR,
      ElectricityMeterChartType.BALANCE_ARITHMETIC,
      ElectricityMeterChartType.BALANCE_HOURLY -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          add(HistoryDataSet.LabelData(icon, "", R.color.on_surface_variant, presentColor = false, useColor = false))
          add(HistoryDataSet.LabelData.forwarded(formatter.format(result.nextSum(), withUnit = false)))
          add(HistoryDataSet.LabelData.reversed(formatter.format(result.nextSum(), withUnit = false)))
        }
      )

      ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          add(HistoryDataSet.LabelData(icon, "", R.color.on_surface_variant, presentColor = false))
          add(HistoryDataSet.LabelData.forwarded(formatter.format(result.nextSum(), withUnit = false)))
          add(HistoryDataSet.LabelData.reversed(formatter.format(result.nextSum(), withUnit = false)))
          add(HistoryDataSet.LabelData(R.color.on_surface_variant))
        }
      )

      else -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          spec.customFilters?.ifPhase1 {
            add(
              HistoryDataSet.LabelData(
                icon,
                formatter.format(result.nextSum(), withUnit = false),
                R.color.phase1
              )
            )
          }
          spec.customFilters?.ifPhase2 {
            add(
              HistoryDataSet.LabelData(
                if (size == 0) icon else null,
                formatter.format(result.nextSum(), withUnit = false),
                R.color.phase2
              )
            )
          }
          spec.customFilters?.ifPhase3 {
            add(
              HistoryDataSet.LabelData(
                if (size == 0) icon else null,
                formatter.format(result.nextSum(), withUnit = false),
                R.color.phase3
              )
            )
          }
        }
      )
    }
  }

  private fun historyDataSet(
    channel: ChannelDataEntity,
    label: HistoryDataSet.Label,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = ChartEntryType.ELECTRICITY,
      label = label,
      valueFormatter = getValueFormatter(ChartEntryType.ELECTRICITY, channel),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  private fun aggregating(measurements: List<ElectricityMeterLogEntity>, spec: ChartDataSpec): AggregationResult {
    val aggregatedEntities = aggregatedEntities(measurements, spec)
    val sum = when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_VECTOR ->
        listOf(measurements.map { (it.faeBalanced ?: 0f) }.sum(), measurements.map { (it.raeBalanced ?: 0f) }.sum())

      ElectricityMeterChartType.BALANCE_ARITHMETIC ->
        listOf(
          measurements.map { (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f) }.sum(),
          measurements.map { (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f) }.sum()
        )

      ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> {
        if (spec.aggregation == ChartDataAggregation.MINUTES) {
          listOf(
            measurements.map { (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f) }.sum(),
            measurements.map { (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f) }.sum()
          )
        } else {
          val formatter = ChartDataAggregation.Formatter()
          val balanced = measurements
            .groupBy { item -> spec.aggregation.aggregator(item.date, formatter) }
            .filter { group -> group.value.isNotEmpty() }
            .map { group ->
              val forwarded = group.value.map { (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f) }.sum()
              val reversed = group.value.map { (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f) }.sum()
              BalancedValue(
                group.value.first().date,
                if (forwarded > reversed) forwarded - reversed else 0f,
                if (reversed > forwarded) reversed - forwarded else 0f
              )
            }
          listOf(balanced.map { it.forwarded }.sum(), balanced.map { it.reversed }.sum())
        }
      }

      ElectricityMeterChartType.BALANCE_HOURLY ->
        measurements.balanceHourly(ChartDataAggregation.Formatter()).let { values ->
          listOf(values.map { it.forwarded }.sum(), values.map { it.reversed }.sum())
        }

      else -> {
        mutableListOf<Float>().apply {
          spec.customFilters?.ifPhase1 { add(measurements.map { (it.phase1.valueFor(spec) ?: 0f) }.sum()) }
          spec.customFilters?.ifPhase2 { add(measurements.map { (it.phase2.valueFor(spec) ?: 0f) }.sum()) }
          spec.customFilters?.ifPhase3 { add(measurements.map { (it.phase3.valueFor(spec) ?: 0f) }.sum()) }
        }
      }
    }

    return AggregationResult(aggregatedEntities, sum)
  }

  private fun aggregatedEntities(measurements: List<ElectricityMeterLogEntity>, spec: ChartDataSpec): List<AggregatedEntity> {
    if (spec.aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map { entity ->
          AggregatedEntity(entity.date.toTimestamp(), AggregatedValue.Multiple(entity.getValues(spec)))
        }
    }

    val formatter = ChartDataAggregation.Formatter()
    return when (val type = (spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_HOURLY -> aggregatedHourly(measurements, spec, formatter)
      else ->
        measurements
          .groupBy { item -> spec.aggregation.aggregator(item.date, formatter) }
          .filter { group -> group.value.isNotEmpty() }
          .map { group ->
            when (type) {
              ElectricityMeterChartType.BALANCE_VECTOR -> aggregatedVectorBalance(spec, group)
              ElectricityMeterChartType.BALANCE_ARITHMETIC -> aggregatedArithmeticBalance(spec, group)
              ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> aggregatedChartBalance(spec, group)
              else -> aggregatedPhases(spec, group)
            }
          }
    }
  }

  private fun aggregatedHourly(
    measurements: List<ElectricityMeterLogEntity>,
    spec: ChartDataSpec,
    formatter: ChartDataAggregation.Formatter
  ): List<AggregatedEntity> =
    balanceHourly(measurements, formatter)
      .groupBy { item -> spec.aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        AggregatedEntity(
          spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          AggregatedValue.Multiple(balanceValues(group.value.map { it.forwarded }.sum(), group.value.map { it.reversed }.sum()))
        )
      }
      .toList()

  private fun balanceHourly(measurements: List<ElectricityMeterLogEntity>, formatter: ChartDataAggregation.Formatter) =
    measurements
      .groupBy { item -> ChartDataAggregation.HOURS.aggregator(item.date, formatter) }
      .asSequence()
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        val consumption =
          group.value.map { it.phase1Fae ?: 0f }.sum() +
            group.value.map { it.phase2Fae ?: 0f }.sum() +
            group.value.map { it.phase3Fae ?: 0f }.sum()
        val production =
          group.value.map { it.phase1Rae ?: 0f }.sum() +
            group.value.map { it.phase2Rae ?: 0f }.sum() +
            group.value.map { it.phase3Rae ?: 0f }.sum()
        val result = consumption - production
        BalancedValue(group.value.firstOrNull()!!.date, if (result > 0) result else 0f, if (result < 0) -result else 0f)
      }

  private fun aggregatedPhases(spec: ChartDataSpec, group: Map.Entry<Long, List<ElectricityMeterLogEntity>>): AggregatedEntity {
    val values = mutableListOf<Float>().apply {
      spec.customFilters?.ifPhase1 { add(group.value.map { it.phase1.valueFor(spec) ?: 0f }.sum()) }
      spec.customFilters?.ifPhase2 { add(group.value.map { it.phase2.valueFor(spec) ?: 0f }.sum()) }
      spec.customFilters?.ifPhase3 { add(group.value.map { it.phase3.valueFor(spec) ?: 0f }.sum()) }
    }

    return AggregatedEntity(
      spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
      AggregatedValue.Multiple(values.toFloatArray())
    )
  }

  private fun aggregatedArithmeticBalance(spec: ChartDataSpec, group: Map.Entry<Long, List<ElectricityMeterLogEntity>>): AggregatedEntity {
    val consumption =
      group.value.map { it.phase1Fae ?: 0f }.sum() +
        group.value.map { it.phase2Fae ?: 0f }.sum() +
        group.value.map { it.phase3Fae ?: 0f }.sum()
    val production =
      group.value.map { it.phase1Rae ?: 0f }.sum() +
        group.value.map { it.phase2Rae ?: 0f }.sum() +
        group.value.map { it.phase3Rae ?: 0f }.sum()

    return AggregatedEntity(
      spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
      AggregatedValue.Multiple(balanceValues(consumption, production))
    )
  }

  private fun aggregatedVectorBalance(spec: ChartDataSpec, group: Map.Entry<Long, List<ElectricityMeterLogEntity>>): AggregatedEntity {
    val consumption = group.value.map { it.faeBalanced ?: 0f }.sum()
    val production = group.value.map { it.raeBalanced ?: 0f }.sum()

    return AggregatedEntity(
      spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
      AggregatedValue.Multiple(balanceValues(consumption, production))
    )
  }

  private fun aggregatedChartBalance(spec: ChartDataSpec, group: Map.Entry<Long, List<ElectricityMeterLogEntity>>): AggregatedEntity {
    val consumption =
      group.value.map { it.phase1Fae ?: 0f }.sum() +
        group.value.map { it.phase2Fae ?: 0f }.sum() +
        group.value.map { it.phase3Fae ?: 0f }.sum()
    val production =
      group.value.map { it.phase1Rae ?: 0f }.sum() +
        group.value.map { it.phase2Rae ?: 0f }.sum() +
        group.value.map { it.phase3Rae ?: 0f }.sum()

    return AggregatedEntity(
      spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
      AggregatedValue.Multiple(chartBalancedValues(consumption, production))
    )
  }

  private data class AggregationResult(
    val list: List<AggregatedEntity>,
    val sum: List<Float>
  ) {
    private var index = 0

    fun nextSum(): Float =
      if (index < sum.count()) {
        sum[index++]
      } else {
        0f
      }
  }
}

fun HistoryDataSet.LabelData.Companion.forwarded(value: String) =
  HistoryDataSet.LabelData(
    fromResource(R.drawable.ic_forward_energy),
    value,
    R.color.chart_color_value_positive,
    iconSize = R.dimen.icon_small_size
  )

fun HistoryDataSet.LabelData.Companion.reversed(value: String) =
  HistoryDataSet.LabelData(
    fromResource(R.drawable.ic_reversed_energy),
    value,
    R.color.chart_color_value_negative,
    iconSize = R.dimen.icon_small_size
  )
