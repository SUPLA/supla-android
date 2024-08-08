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
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChannelSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toTimestamp
import org.supla.android.features.details.detailbase.history.ui.CheckboxItem
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ElectricityConsumptionMeasurementsProvider @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  preferences: Preferences,
  @Named(GSON_FOR_REPO) gson: Gson
) : ChannelMeasurementsProvider(getChannelValueStringUseCase, getChannelIconUseCase, preferences, gson) {

  override fun handle(function: Int) = function == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER

  override fun provide(
    channel: ChannelDataEntity,
    spec: ChartDataSpec,
    colorProvider: ((ChartEntryType) -> Int)?
  ): Single<ChannelSets> =
    electricityMeterLogRepository.findMeasurements(channel.remoteId, channel.profileId, spec.startDate, spec.endDate)
      .map { aggregating(it, spec) }
      .map { listOf(historyDataSet(channel, labels(spec, getChannelIconUseCase.getIconProvider(channel)), spec.aggregation, it)) }
      .map { ChannelSets(channel, getChannelCaptionUseCase(channel.channelEntity), spec.aggregation, it) }
      .firstOrError()

  private fun labels(spec: ChartDataSpec, icon: BitmapProvider): HistoryDataSet.Label =
    HistoryDataSet.Label.Multiple(
      mutableListOf<HistoryDataSet.LabelData>().apply {
        spec.ifPhase1 { add(HistoryDataSet.LabelData(icon, "F1", R.color.phase1)) }
        spec.ifPhase2 { add(HistoryDataSet.LabelData(if (size == 0) icon else null, "F2", R.color.phase2)) }
        spec.ifPhase3 { add(HistoryDataSet.LabelData(if (size == 0) icon else null, "F3", R.color.phase3)) }
      }
    )

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

  private fun aggregating(measurements: List<ElectricityMeterLogEntity>, spec: ChartDataSpec): List<AggregatedEntity> {
    if (spec.aggregation == ChartDataAggregation.MINUTES) {
      return measurements
        .map { entity ->
          AggregatedEntity(entity.date.toTimestamp(), AggregatedValue.Multiple(entity.getValues(spec)))
        }
    }

    val formatter = ChartDataAggregation.Formatter()
    return measurements
      .groupBy { item -> spec.aggregation.aggregator(item.date, formatter) }
      .filter { group -> group.value.isNotEmpty() }
      .map { group ->
        val values = mutableListOf<Float>().apply {
          spec.ifPhase1 { add(group.value.map { it.phase1.valueFor(spec) ?: 0f }.reduce { acc, phaseValues -> acc + phaseValues }) }
          spec.ifPhase2 { add(group.value.map { it.phase2.valueFor(spec) ?: 0f }.reduce { acc, phaseValues -> acc + phaseValues }) }
          spec.ifPhase3 { add(group.value.map { it.phase3.valueFor(spec) ?: 0f }.reduce { acc, phaseValues -> acc + phaseValues }) }
        }

        AggregatedEntity(
          spec.aggregation.groupTimeProvider(group.value.firstOrNull()!!.date),
          AggregatedValue.Multiple(values.toFloatArray())
        )
      }
  }
}

private fun ChartDataSpec.ifPhase1(callback: () -> Unit) {
  if ((customFilters as? ElectricityChartFilters)?.phases?.contains(PhaseItem.PHASE_1) != false) {
    callback()
  }
}

private fun ChartDataSpec.ifPhase2(callback: () -> Unit) {
  if ((customFilters as? ElectricityChartFilters)?.phases?.contains(PhaseItem.PHASE_2) != false) {
    callback()
  }
}

private fun ChartDataSpec.ifPhase3(callback: () -> Unit) {
  if ((customFilters as? ElectricityChartFilters)?.phases?.contains(PhaseItem.PHASE_3) != false) {
    callback()
  }
}

fun ElectricityMeterLogEntity.getValues(spec: ChartDataSpec): FloatArray {
  val (filters) = guardLet((spec.customFilters as? ElectricityChartFilters)) {
    return mutableListOf<Float>()
      .apply {
        phase1.fae?.let { add(it) }
        phase2.fae?.let { add(it) }
        phase3.fae?.let { add(it) }
      }
      .toFloatArray()
  }

  return mutableListOf<Float>()
    .apply {
      if (filters.phases.contains(PhaseItem.PHASE_1)) {
        phase1.valueFor(filters.type)?.let { add(it) }
      }
      if (filters.phases.contains(PhaseItem.PHASE_2)) {
        phase2.valueFor(filters.type)?.let { add(it) }
      }
      if (filters.phases.contains(PhaseItem.PHASE_3)) {
        phase3.valueFor(filters.type)?.let { add(it) }
      }
    }
    .toFloatArray()
}

data class ElectricityChartFilters(
  val type: ElectricityMeterChartType,
  val phases: Set<PhaseItem>
) : ChartDataSpec.Filters

data class PhaseItem(
  override val color: Int,
  override val label: Int
) : CheckboxItem {

  companion object {
    val PHASE_1 = PhaseItem(R.color.phase1, R.string.em_phase1)
    val PHASE_2 = PhaseItem(R.color.phase2, R.string.em_phase2)
    val PHASE_3 = PhaseItem(R.color.phase3, R.string.em_phase3)
    val ALL = listOf(PHASE_1, PHASE_2, PHASE_3)
  }
}
