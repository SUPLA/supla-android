package org.supla.android.features.details.thermostatdetail.history
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
import io.reactivex.rxjava3.core.Observable
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
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelWithChildrenMeasurementsDateRangeUseCase
import org.supla.android.usecases.channel.LoadChannelWithChildrenMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import javax.inject.Inject

@HiltViewModel
class ThermostatHistoryDetailViewModel @Inject constructor(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val loadChannelWithChildrenMeasurementsUseCase: LoadChannelWithChildrenMeasurementsUseCase,
  private val loadChannelWithChildrenMeasurementsDateRangeUseCase: LoadChannelWithChildrenMeasurementsDateRangeUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val profileManager: ProfileManager,
  private val userStateHolder: UserStateHolder,
  deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseHistoryDetailViewModel(deleteChannelMeasurementsUseCase, userStateHolder, dateProvider, schedulers) {

  override fun triggerDataLoad(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId),
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
    chartRange: ChartRange,
  ): Single<Pair<ChartData, Optional<DateRange>>> =
    Single.zip(
      loadChannelWithChildrenMeasurementsUseCase(remoteId, spec),
      loadChannelWithChildrenMeasurementsDateRangeUseCase(remoteId, profileId)
    ) { first, second -> Pair(LineChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, first), second) }

  private fun handleData(channel: ChannelWithChildren, chartState: ChartState) {
    updateState { it.copy(profileId = channel.channel.channelEntity.profileId, channelFunction = channel.channel.function.value) }

    if (channel.children.none { it.channelRelationEntity.relationType.isThermometer() }) {
      updateState { it.copy(loading = false) }
    } else {
      restoreRange(chartState)
      configureDownloadObserver(channel)
      startInitialDataLoad(channel)
    }
  }

  private fun startInitialDataLoad(channel: ChannelWithChildren) {
    if (currentState().initialLoadStarted) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(initialLoadStarted = true) }

    channel.children.firstOrNull { it.relationType.isMainThermometer() }?.let {
      downloadChannelMeasurementsUseCase.invoke(it.withChildren)
    }
    channel.children.firstOrNull { it.relationType.isAuxThermometer() }?.let {
      downloadChannelMeasurementsUseCase.invoke(it.withChildren)
    }
  }

  private fun configureDownloadObserver(channel: ChannelWithChildren) {
    if (currentState().downloadConfigured) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(downloadConfigured = true) }

    val mainThermometerId = channel.children.firstOrNull { it.relationType.isMainThermometer() }?.channel?.remoteId
    val auxThermometerId = channel.children.firstOrNull { it.relationType.isAuxThermometer() }?.channel?.remoteId

    val observables = mutableListOf<Observable<DownloadEventsManager.State>>()
    mainThermometerId?.let { observables.add(downloadEventsManager.observeProgress(it)) }
    auxThermometerId?.let { observables.add(downloadEventsManager.observeProgress(it)) }

    val observable = if (observables.count() == 2) {
      Observable.combineLatest(observables[0], observables[1]) { first, second ->
        Pair<DownloadEventsManager.State, DownloadEventsManager.State?>(first, second)
      }
    } else if (observables.count() == 1) {
      observables[0].map { Pair<DownloadEventsManager.State, DownloadEventsManager.State?>(it, null) }
    } else {
      Observable.empty()
    }

    observable.attachSilent()
      .map { mergeEvents(it.first, it.second) }
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { handleDownloadEvents(it) },
        onError = defaultErrorHandler("configureDownloadObserver")
      )
      .disposeBySelf()
  }
}
