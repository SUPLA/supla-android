package org.supla.android.features.details.gpmdetail.history
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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.datatype.BarChartData
import org.supla.android.data.model.chart.datatype.CandleChartData
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.LineChartData
import org.supla.android.data.model.chart.style.GpmChartStyle
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeasurementChartType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterChartType
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.ifLet
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsDataRangeUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.usecases.migration.GroupingStringMigrationUseCase
import org.supla.core.shared.data.model.rest.channel.ChannelDto
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class GpmHistoryDetailViewModel @Inject constructor(
  private val loadChannelMeasurementsDataRangeUseCase: LoadChannelMeasurementsDataRangeUseCase,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsUseCase: LoadChannelMeasurementsUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  @Named(GSON_FOR_REPO) private val gson: Gson,
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

  override fun loadData(remoteId: Int) {
    super.loadData(remoteId)

    channelConfigEventsManager.observerConfig(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = { reloadMeasurements() },
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

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
        Single.zip(
          loadChannelMeasurementsUseCase(remoteId, correctedSpec),
          loadChannelConfigUseCase(profileId, remoteId)
        ) { sets, config ->
          Pair(createChartData(listOf(sets), DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, config), range)
        }
      }

  fun reloadMeasurements() {
    val state = currentState()
    // Config check is needed to verify if history is still allowed. In case of change we need to update view appropriate
    loadChannelConfigUseCase(state.profileId, state.remoteId)
      .attachSilent()
      .subscribeBy(
        onSuccess = {
          ifLet(it as? SuplaChannelGeneralPurposeBaseConfig) { (config) ->
            updateState { state ->
              triggerMeasurementsLoad(state)
              state.copy(
                showHistory = config.keepHistory,
                downloadState = DownloadEventsManager.State.Finished
              )
            }
          }
        },
        onError = defaultErrorHandler("triggerMeasurementsLoad")
      )
      .disposeBySelf()
  }

  override fun handleData(channelWithChildren: ChannelWithChildren, channelDto: ChannelDto, chartState: ChartState) {
    val channel = channelWithChildren.channel
    updateState {
      it.copy(
        profileId = channel.channelEntity.profileId,
        channelFunction = channel.function.value,
        chartStyle = GpmChartStyle
      )
    }

    restoreRange(chartState)
    if ((channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelGeneralPurposeBaseConfig)?.keepHistory == true) {
      configureDownloadObserver(channel.channelEntity.remoteId)
      startInitialDataLoad(channelWithChildren)
    } else {
      updateState { state ->
        triggerMeasurementsLoad(state)
        state.copy(
          showHistory = false,
          downloadState = DownloadEventsManager.State.Finished
        )
      }
    }
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

  private fun createChartData(
    sets: List<ChannelChartSets>,
    dateRange: DateRange,
    chartRange: ChartRange,
    aggregation: ChartDataAggregation,
    config: SuplaChannelConfig
  ): ChartData =
    when (config) {
      is SuplaChannelGeneralPurposeMeterConfig -> createChartData(sets, dateRange, chartRange, aggregation, config)
      is SuplaChannelGeneralPurposeMeasurementConfig -> createChartData(sets, dateRange, chartRange, aggregation, config)
      else -> LineChartData(dateRange, chartRange, aggregation, sets)
    }

  private fun createChartData(
    sets: List<ChannelChartSets>,
    dateRange: DateRange,
    chartRange: ChartRange,
    aggregation: ChartDataAggregation,
    config: SuplaChannelGeneralPurposeMeterConfig
  ): ChartData =
    when (config.chartType) {
      SuplaChannelConfigMeterChartType.BAR -> BarChartData(dateRange, chartRange, aggregation, sets)
      SuplaChannelConfigMeterChartType.LINEAR -> LineChartData(dateRange, chartRange, aggregation, sets)
    }

  private fun createChartData(
    sets: List<ChannelChartSets>,
    dateRange: DateRange,
    chartRange: ChartRange,
    aggregation: ChartDataAggregation,
    config: SuplaChannelGeneralPurposeMeasurementConfig
  ): ChartData =
    when (config.chartType) {
      SuplaChannelConfigMeasurementChartType.BAR -> BarChartData(dateRange, chartRange, aggregation, sets)
      SuplaChannelConfigMeasurementChartType.LINEAR -> LineChartData(dateRange, chartRange, aggregation, sets)
      SuplaChannelConfigMeasurementChartType.CANDLE -> CandleChartData(dateRange, chartRange, aggregation, sets)
    }
}
