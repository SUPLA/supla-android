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
import org.supla.android.Preferences
import org.supla.android.core.shared.provider
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class CurrentChartCustomData(
  val phases: List<Phase>
)

@Singleton
class CurrentMeasurementsProvider @Inject constructor(
  private val currentLogRepository: CurrentLogRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  @Named(GSON_FOR_REPO) gson: Gson,
  preferences: Preferences
) : ElectricityMeasurementsProvider<CurrentHistoryLogEntity>(getChannelIconUseCase, gson, preferences) {

  override val labelValueExtractor: (SuplaChannelElectricityMeterValue.Measurement?) -> Double
    get() = { it?.current ?: 0.0 }

  operator fun invoke(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec
  ): Single<ChannelChartSets> {
    val observables: MutableList<Observable<Pair<Phase, HistoryDataSet>>> = mutableListOf()
    spec.customFilters?.ifPhase1 {
      observables.add(findMeasurementsForPhase(channelWithChildren, spec, observables.isEmpty(), Phase.PHASE_1))
    }
    spec.customFilters?.ifPhase2 {
      observables.add(findMeasurementsForPhase(channelWithChildren, spec, observables.isEmpty(), Phase.PHASE_2))
    }
    spec.customFilters?.ifPhase3 {
      observables.add(findMeasurementsForPhase(channelWithChildren, spec, observables.isEmpty(), Phase.PHASE_3))
    }

    val channel = channelWithChildren.channel
    return Observable.zip(
      observables
    ) { it.filterIsInstance<Pair<Phase, HistoryDataSet>>() }
      .map { historyDataSets ->
        ChannelChartSets(
          channel,
          getCaptionUseCase(channel.shareable).provider(),
          spec.aggregation,
          historyDataSets.map { it.second },
          CurrentChartCustomData(historyDataSets.map { it.first }),
          (spec.customFilters as? ElectricityChartFilters)?.type?.label
        )
      }
      .firstOrError()
  }

  private fun findMeasurementsForPhase(
    channelWithChildren: ChannelWithChildren,
    spec: ChartDataSpec,
    isFirst: Boolean,
    phase: Phase
  ): Observable<Pair<Phase, HistoryDataSet>> =
    currentLogRepository.findMeasurements(channelWithChildren.remoteId, channelWithChildren.profileId, spec.startDate, spec.endDate, phase)
      .map { aggregating(it, spec.aggregation) }
      .map { Pair(phase, historyDataSet(channelWithChildren, phase, isFirst, ChartEntryType.CURRENT, spec.aggregation, it)) }
}
