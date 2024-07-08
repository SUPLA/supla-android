package org.supla.android.features.details.electricitymeterdetail.general
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.monthStart
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterGeneralStateHandler
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.electricitymeter.ElectricityMeasurements
import org.supla.android.usecases.channel.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import javax.inject.Inject

@HiltViewModel
class ElectricityMeterGeneralViewModel @Inject constructor(
  private val loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase,
  private val electricityMeterGeneralStateHandler: ElectricityMeterGeneralStateHandler,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<ElectricityMeterGeneralViewModelState, ElectricityMeterGeneralViewEvent>(
  ElectricityMeterGeneralViewModelState(),
  schedulers
) {

  fun onViewCreated(remoteId: Int) {
    observeDownload(remoteId)
  }

  fun loadData(remoteId: Int, cleanupDownloading: Boolean = false) {
    Maybe.zip(
      readChannelByRemoteIdUseCase(remoteId),
      loadElectricityMeterMeasurementsUseCase(remoteId, dateProvider.currentDate().monthStart())
    ) { channel, measurements -> Pair(channel, measurements) }
      .attach()
      .subscribeBy(
        onSuccess = { (channel, measurements) -> handleChannel(channel, measurements, cleanupDownloading) },
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channel: ChannelDataEntity, measurements: ElectricityMeasurements, cleanupDownloading: Boolean) {
    updateState { state ->
      if (!state.initialDataLoadStarted) {
        downloadChannelMeasurementsUseCase.invoke(channel.remoteId, channel.profileId, channel.function.value)
      }

      electricityMeterGeneralStateHandler.updateState(state.viewState.electricityMeterState, channel, measurements)?.let {
        val downloading = if (cleanupDownloading) false else state.viewState.electricityMeterState.currentMonthDownloading
        state.copy(
          remoteId = channel.remoteId,
          initialDataLoadStarted = true,
          viewState = state.viewState.copy(electricityMeterState = it.copy(currentMonthDownloading = downloading))
        )
      } ?: state
    }
  }

  private fun observeDownload(remoteId: Int) {
    downloadEventsManager.observeProgress(remoteId).attachSilent()
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { handleDownloadEvents(it) },
        onError = defaultErrorHandler("configureDownloadObserver")
      )
      .disposeBySelf()
  }

  private fun handleDownloadEvents(downloadState: DownloadEventsManager.State) {
    when (downloadState) {
      is DownloadEventsManager.State.InProgress,
      is DownloadEventsManager.State.Started -> {
        updateState {
          it.copy(
            viewState = it.viewState.copy(
              electricityMeterState = it.viewState.electricityMeterState.copy(currentMonthDownloading = true)
            )
          )
        }
      }

      else -> {
        loadData(currentState().remoteId, cleanupDownloading = true)
      }
    }
  }
}

sealed class ElectricityMeterGeneralViewEvent : ViewEvent

data class ElectricityMeterGeneralViewModelState(
  val remoteId: Int = 0,
  val initialDataLoadStarted: Boolean = false,
  val viewState: ElectricityMeterGeneralViewState = ElectricityMeterGeneralViewState()
) : ViewState()
