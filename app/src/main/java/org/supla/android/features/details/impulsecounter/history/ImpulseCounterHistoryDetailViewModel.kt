package org.supla.android.features.details.impulsecounter.history
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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.ImpulseBarChartData
import org.supla.android.data.model.chart.datatype.PieChartData
import org.supla.android.data.model.chart.style.ImpulseCounterChartStyle
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.features.details.detailbase.history.ui.ChartDataSelectionDialogState
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsDataRangeUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.migration.GroupingStringMigrationUseCase
import org.supla.core.shared.data.model.rest.channel.ChannelDto
import javax.inject.Inject

@HiltViewModel
class ImpulseCounterHistoryDetailViewModel @Inject constructor(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsUseCase: LoadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsDataRangeUseCase: LoadChannelMeasurementsDataRangeUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  groupingStringMigrationUseCase: GroupingStringMigrationUseCase,
  userStateHolder: UserStateHolder,
  profileManager: ProfileManager,
  schedulers: SuplaSchedulers,
  dateProvider: DateProvider
) : BaseHistoryDetailViewModel(
  deleteChannelMeasurementsUseCase,
  readChannelWithChildrenUseCase,
  groupingStringMigrationUseCase,
  userStateHolder,
  profileManager,
  dateProvider,
  schedulers
) {

  override fun allAggregations() = ChartDataAggregation.entries

  override fun provideSelectionDialogState(
    channelChartSets: ChannelChartSets,
    customFilters: ChartDataSpec.Filters?
  ): ChartDataSelectionDialogState? = null

  override fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    spec: ChartDataSpec,
    chartRange: ChartRange
  ): Single<Pair<ChartData, Optional<DateRange>>> =
    loadChannelMeasurementsDataRangeUseCase(remoteId, profileId)
      .flatMap { range ->
        // while the data range is changing (voltage, current, power active has different range) it has to be corrected
        val correctedSpec = if (chartRange == ChartRange.ALL_HISTORY) spec.correctBy(range) else spec
        loadChannelMeasurementsUseCase(remoteId, correctedSpec)
          .map { Pair(getChartData(correctedSpec, chartRange, it), range) }
      }

  override fun handleData(channelWithChildren: ChannelWithChildren, channelDto: ChannelDto, chartState: ChartState) {
    val channel = channelWithChildren.channel
    updateState { it.copy(profileId = channel.profileId, channelFunction = channel.function.value, chartStyle = ImpulseCounterChartStyle) }

    restoreRange(chartState)
    configureDownloadObserver(channel.remoteId)
    startInitialDataLoad(channelWithChildren)
  }

  private fun startInitialDataLoad(channelWithChildren: ChannelWithChildren) {
    if (currentState().initialLoadStarted) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(initialLoadStarted = true) }
    downloadChannelMeasurementsUseCase.invoke(channelWithChildren)
  }

  private fun configureDownloadObserver(remoteId: Int) {
    if (currentState().downloadConfigured) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(downloadConfigured = true) }

    downloadEventsManager.observeProgress(remoteId).attachSilent()
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { handleDownloadEvents(it) },
        onError = defaultErrorHandler("configureDownloadObserver")
      )
      .disposeBySelf()
  }

  private fun getChartData(spec: ChartDataSpec, chartRange: ChartRange, sets: ChannelChartSets): ChartData {
    return if (spec.aggregation.isRank) {
      PieChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(sets))
    } else {
      ImpulseBarChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(sets))
    }
  }
}
