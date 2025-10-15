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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.R
import org.supla.android.core.shared.provider
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
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
import org.supla.android.data.source.local.entity.custom.BalancedValue
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.toTimestamp
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.images.ImageId
import org.supla.android.ui.views.charts.marker.ElectricityMarkerCustomData
import org.supla.android.usecases.channel.measurementsprovider.AggregationResult
import org.supla.android.usecases.channel.measurementsprovider.MeasurementsProvider
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ElectricityConsumptionProvider @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  @Named(GSON_FOR_REPO) gson: Gson,
  preferences: ApplicationPreferences
) : MeasurementsProvider(preferences, gson) {

  private val formatter = ElectricityMeterValueFormatter(ValueFormatSpecification.ElectricityMeterForChartSummary)

  operator fun invoke(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec
  ): Single<ChannelChartSets> {
    val channel = channelWithChildren.channel
    return getGroupedMeasurements(channel, spec)
      .map { listOf(historyDataSet(channelWithChildren, labels(spec, getChannelIconUseCase(channel), it), spec.aggregation, it.list)) }
      .map { historyDataSets ->
        ChannelChartSets(
          channel,
          getCaptionUseCase(channel.shareable).provider(),
          spec.aggregation,
          historyDataSets,
          ElectricityMarkerCustomData(
            spec.customFilters as? ElectricityChartFilters,
            channel.Electricity.value?.pricePerUnit?.toFloat(),
            channel.Electricity.value?.currency
          ),
          (spec.customFilters as? ElectricityChartFilters)?.type?.labelWithUnit
        )
      }
      .firstOrError()
  }

  private fun getGroupedMeasurements(channel: ChannelDataEntity, spec: ChartDataSpec): Observable<AggregationResult> =
    when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_HOURLY ->
        electricityMeterLogRepository.findMeasurementsHourlyGrouped(
          channel.remoteId,
          channel.profileId,
          spec.startDate,
          spec.endDate,
          spec.aggregation.groupingStringStartPosition,
          spec.aggregation.groupingStringLength
        )
          .map { aggregatingBalancedValues(it, spec) }

      else ->
        electricityMeterLogRepository.findMeasurementsGrouped(
          channel.remoteId,
          channel.profileId,
          spec.startDate,
          spec.endDate,
          spec.aggregation.groupingStringStartPosition,
          spec.aggregation.groupingStringLength
        )
          .map { aggregating(it, spec) }
    }

  private fun labels(spec: ChartDataSpec, icon: ImageId, result: AggregationResult): HistoryDataSet.Label {
    return when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_VECTOR,
      ElectricityMeterChartType.BALANCE_ARITHMETIC,
      ElectricityMeterChartType.BALANCE_HOURLY -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          add(HistoryDataSet.LabelData(icon, "", R.color.on_surface_variant, presentColor = false, useColor = false))
          add(HistoryDataSet.LabelData.forwarded(formatter.format(result.nextSum())))
          add(HistoryDataSet.LabelData.reversed(formatter.format(result.nextSum())))
        }
      )

      ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          add(HistoryDataSet.LabelData(icon, "", R.color.on_surface_variant, presentColor = false))
          add(HistoryDataSet.LabelData.forwarded(formatter.format(result.nextSum())))
          add(HistoryDataSet.LabelData.reversed(formatter.format(result.nextSum())))
          add(HistoryDataSet.LabelData(R.color.on_surface_variant))
        }
      )

      else -> HistoryDataSet.Label.Multiple(
        mutableListOf<HistoryDataSet.LabelData>().apply {
          spec.customFilters?.ifPhase1 {
            add(
              HistoryDataSet.LabelData(
                icon,
                formatter.format(result.nextSum()),
                R.color.phase1
              )
            )
          }
          spec.customFilters?.ifPhase2 {
            add(
              HistoryDataSet.LabelData(
                if (size == 0) icon else null,
                formatter.format(result.nextSum()),
                R.color.phase2
              )
            )
          }
          spec.customFilters?.ifPhase3 {
            add(
              HistoryDataSet.LabelData(
                if (size == 0) icon else null,
                formatter.format(result.nextSum()),
                R.color.phase3
              )
            )
          }
        }
      )
    }
  }

  private fun historyDataSet(
    channelWithChildren: ChannelWithChildren,
    label: HistoryDataSet.Label,
    aggregation: ChartDataAggregation,
    measurements: List<AggregatedEntity>
  ): HistoryDataSet =
    HistoryDataSet(
      type = ChartEntryType.ELECTRICITY,
      label = label,
      valueFormatter = getValueFormatter(ChartEntryType.ELECTRICITY, channelWithChildren),
      entities = divideSetToSubsets(
        entities = measurements,
        aggregation = aggregation
      )
    )

  private fun aggregatingBalancedValues(measurements: List<BalancedValue>, spec: ChartDataSpec): AggregationResult {
    val aggregatedEntities = measurements.map {
      AggregatedEntity(
        spec.aggregation.groupTimeProvider(it.date),
        AggregatedValue.Multiple(balanceValues(it.forwarded, it.reversed))
      )
    }

    return AggregationResult(
      aggregatedEntities,
      listOf(measurements.map { it.forwarded }.sum(), measurements.map { it.reversed }.sum())
    )
  }

  private fun aggregating(measurements: List<ElectricityMeterLogEntity>, spec: ChartDataSpec): AggregationResult {
    val aggregatedEntities = aggregatedGroupedEntities(measurements, spec)
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
          val balanced = measurements
            .map {
              val forwarded = (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f)
              val reversed = (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f)

              BalancedValue(
                it.date,
                it.groupingString,
                if (forwarded > reversed) forwarded - reversed else 0f,
                if (reversed > forwarded) reversed - forwarded else 0f
              )
            }
          listOf(balanced.map { it.forwarded }.sum(), balanced.map { it.reversed }.sum())
        }
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

  private fun aggregatedGroupedEntities(measurements: List<ElectricityMeterLogEntity>, spec: ChartDataSpec): List<AggregatedEntity> {
    if (spec.aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map { entity ->
          AggregatedEntity(entity.date.toTimestamp(), AggregatedValue.Multiple(entity.getValues(spec)))
        }
    }

    return when ((spec.customFilters as? ElectricityChartFilters)?.type) {
      ElectricityMeterChartType.BALANCE_VECTOR -> aggregatedVectorBalance(spec, measurements)
      ElectricityMeterChartType.BALANCE_ARITHMETIC -> aggregatedArithmeticBalance(spec, measurements)
      ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> aggregatedChartBalance(spec, measurements)
      else -> aggregatedPhases(spec, measurements)
    }
      .let { list ->
        if (spec.aggregation.isRank) {
          list.sortedByDescending { it.value.valueMax }
        } else {
          list
        }
      }
  }

  private fun aggregatedPhases(
    spec: ChartDataSpec,
    entities: List<ElectricityMeterLogEntity>
  ): List<AggregatedEntity> {
    return entities.map {
      val value = if (spec.aggregation.isRank) {
        var sum = 0f
        spec.customFilters?.ifPhase1 { sum += it.phase1.valueFor(spec) ?: 0f }
        spec.customFilters?.ifPhase2 { sum += it.phase2.valueFor(spec) ?: 0f }
        spec.customFilters?.ifPhase3 { sum += it.phase3.valueFor(spec) ?: 0f }
        AggregatedValue.Single(sum)
      } else {
        AggregatedValue.Multiple(
          mutableListOf<Float>().apply {
            spec.customFilters?.ifPhase1 { add(it.phase1.valueFor(spec) ?: 0f) }
            spec.customFilters?.ifPhase2 { add(it.phase2.valueFor(spec) ?: 0f) }
            spec.customFilters?.ifPhase3 { add(it.phase3.valueFor(spec) ?: 0f) }
          }.toFloatArray()
        )
      }

      AggregatedEntity(
        if (spec.aggregation.isRank) it.groupingString.toLong() else spec.aggregation.groupTimeProvider(it.date),
        value
      )
    }
  }

  private fun aggregatedArithmeticBalance(spec: ChartDataSpec, entities: List<ElectricityMeterLogEntity>): List<AggregatedEntity> =
    entities.map { entity ->
      val consumption = (entity.phase1Fae ?: 0f) + (entity.phase2Fae ?: 0f) + (entity.phase3Fae ?: 0f)
      val production = (entity.phase1Rae ?: 0f) + (entity.phase2Rae ?: 0f) + (entity.phase3Rae ?: 0f)

      AggregatedEntity(
        spec.aggregation.groupTimeProvider(entity.date),
        AggregatedValue.Multiple(balanceValues(consumption, production))
      )
    }

  private fun aggregatedVectorBalance(spec: ChartDataSpec, entities: List<ElectricityMeterLogEntity>): List<AggregatedEntity> =
    entities.map { entity ->
      val consumption = entity.faeBalanced ?: 0f
      val production = entity.raeBalanced ?: 0f

      AggregatedEntity(
        spec.aggregation.groupTimeProvider(entity.date),
        AggregatedValue.Multiple(balanceValues(consumption, production))
      )
    }

  private fun aggregatedChartBalance(spec: ChartDataSpec, entities: List<ElectricityMeterLogEntity>): List<AggregatedEntity> =
    entities.map { entity ->
      val consumption = (entity.phase1Fae ?: 0f) + (entity.phase2Fae ?: 0f) + (entity.phase3Fae ?: 0f)
      val production = (entity.phase1Rae ?: 0f) + (entity.phase2Rae ?: 0f) + (entity.phase3Rae ?: 0f)

      AggregatedEntity(
        spec.aggregation.groupTimeProvider(entity.date),
        AggregatedValue.Multiple(chartBalancedValues(consumption, production))
      )
    }
}

fun HistoryDataSet.LabelData.Companion.forwarded(value: String) =
  HistoryDataSet.LabelData(
    ImageId(R.drawable.ic_forward_energy),
    value,
    R.color.chart_color_value_positive,
    iconSize = R.dimen.icon_small_size
  )

fun HistoryDataSet.LabelData.Companion.reversed(value: String) =
  HistoryDataSet.LabelData(
    ImageId(R.drawable.ic_reversed_energy),
    value,
    R.color.chart_color_value_negative,
    iconSize = R.dimen.icon_small_size
  )
