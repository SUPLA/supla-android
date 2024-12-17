package org.supla.android.features.details.humiditydetail.history
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
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.LineChartData
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.data.model.chart.style.HumidityChartStyle
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.events.DownloadEventsManager
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsDataRangeUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject

@HiltViewModel
class HumidityHistoryDetailViewModel @Inject constructor(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsUseCase: LoadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsDataRangeUseCase: LoadChannelMeasurementsDataRangeUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val profileManager: ProfileManager,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  userStateHolder: UserStateHolder,
  deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseHistoryDetailViewModel(deleteChannelMeasurementsUseCase, userStateHolder, dateProvider, schedulers) {

  override fun chartStyle(): ChartStyle = HumidityChartStyle

  override fun triggerDataLoad(remoteId: Int) {
    Maybe.zip(
      readChannelByRemoteIdUseCase(remoteId),
      profileManager.getCurrentProfile().map { loadChartState(it.id, remoteId) }
    ) { first, second ->
      Pair(first, second)
    }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it.first, it.second) },
        onError = defaultErrorHandler("triggerDataLoad")
      )
      .disposeBySelf()
  }

  override fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    spec: ChartDataSpec,
    chartRange: ChartRange
  ): Single<Pair<ChartData, Optional<DateRange>>> =
    Single.zip(
      loadChannelMeasurementsUseCase(remoteId, spec),
      loadChannelMeasurementsDataRangeUseCase(remoteId, profileId)
    ) { first, second -> Pair(LineChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(first)), second) }

  private fun handleData(channel: ChannelDataEntity, chartState: ChartState) {
    updateState { it.copy(profileId = channel.profileId, channelFunction = channel.function.value) }

    restoreRange(chartState)
    configureDownloadObserver(channel.remoteId)
    startInitialDataLoad(channel.remoteId, channel.profileId, channel.function)
  }

  private fun startInitialDataLoad(remoteId: Int, profileId: Long, channelFunction: SuplaFunction) {
    if (currentState().initialLoadStarted) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(initialLoadStarted = true) }
    downloadChannelMeasurementsUseCase.invoke(remoteId, profileId, channelFunction)
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
}
