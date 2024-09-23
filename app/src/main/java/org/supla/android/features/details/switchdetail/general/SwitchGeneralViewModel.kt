package org.supla.android.features.details.switchdetail.general
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

import android.text.format.DateFormat
import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.model.general.hasElectricityMeter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.ifTrue
import org.supla.android.extensions.monthStart
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterGeneralStateHandler
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterState
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.electricitymeter.ElectricityMeasurements
import org.supla.android.usecases.channel.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SwitchGeneralViewModel @Inject constructor(
  private val loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase,
  private val electricityMeterGeneralStateHandler: ElectricityMeterGeneralStateHandler,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<SwitchGeneralViewState, SwitchGeneralViewEvent>(SwitchGeneralViewState(), schedulers) {

  fun onViewCreated(remoteId: Int) {
    observeDownload(remoteId)
  }

  fun loadData(remoteId: Int, itemType: ItemType, cleanupDownloading: Boolean = false) {
    getDataSource(remoteId, itemType)
      .flatMap { channelBase ->
        if (channelBase.hasElectricityMeter) {
          loadElectricityMeterMeasurementsUseCase(remoteId, dateProvider.currentDate().monthStart())
            .map { Pair(channelBase, it) }
        } else {
          Maybe.just(Pair<ChannelDataBase, ElectricityMeasurements?>(channelBase, null))
        }
      }
      .attachSilent()
      .subscribeBy(
        onSuccess = { (channelBase, measurements) -> handleData(channelBase, measurements, cleanupDownloading) },
        onError = defaultErrorHandler("loadData($remoteId, $itemType)")
      )
      .disposeBySelf()
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelByRemoteIdUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }

  fun turnOn(remoteId: Int, itemType: ItemType) {
    performAction(ActionId.TURN_ON, itemType, remoteId)
  }

  fun turnOff(remoteId: Int, itemType: ItemType) {
    performAction(ActionId.TURN_OFF, itemType, remoteId)
  }

  private fun performAction(actionId: ActionId, itemType: ItemType, remoteId: Int) {
    executeSimpleActionUseCase(actionId, itemType.subjectType, remoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performAction($actionId, $itemType, $remoteId)"))
      .disposeBySelf()
  }

  private fun handleData(data: ChannelDataBase, measurements: ElectricityMeasurements?, cleanupDownloading: Boolean) {
    updateState { state ->
      if (data.hasElectricityMeter && !state.initialDataLoadStarted) {
        downloadChannelMeasurementsUseCase.invoke(data.remoteId, data.profileId, data.function.value)
      }

      val downloading = if (cleanupDownloading) false else (state.electricityMeterState?.currentMonthDownloading ?: false)
      val showButtons = data.function.switchWithButtons

      state.copy(
        remoteId = data.remoteId,
        itemType = if (data is ChannelGroupDataEntity) ItemType.GROUP else ItemType.CHANNEL,
        online = data.isOnline(),
        initialDataLoadStarted = true,
        deviceStateLabel = getDeviceStateLabel(data),
        deviceStateIcon = getChannelIconUseCase(data),
        deviceStateValue = getDeviceStateValue(data),
        showButtons = showButtons,
        onIcon = showButtons.ifTrue(getChannelIconUseCase(data, channelStateValue = ChannelState.Value.ON)),
        offIcon = showButtons.ifTrue(getChannelIconUseCase(data, channelStateValue = ChannelState.Value.OFF)),
        electricityMeterState = electricityMeterGeneralStateHandler
          .updateState(state.electricityMeterState, data, measurements)
          ?.copy(currentMonthDownloading = downloading)
      )
    }
  }

  private fun getDeviceStateLabel(data: ChannelDataBase): StringProvider {
    return getEstimatedCountDownEndTime(data)
      ?.let { estimatedCountDownEndTime ->
        { context ->
          val format = context.getString(R.string.hour_string_format)
          context.getString(R.string.details_timer_state_label_for_timer, DateFormat.format(format, estimatedCountDownEndTime))
        }
      } ?: { context -> context.getString(R.string.details_timer_state_label) }
  }

  private fun getEstimatedCountDownEndTime(channelDataBase: ChannelDataBase): Date? {
    return (channelDataBase as? ChannelDataEntity)?.let {
      val currentDate = dateProvider.currentDate()
      val estimatedEndDate = it.channelExtendedValueEntity?.getSuplaValue()?.timerEstimatedEndDate

      if (estimatedEndDate?.after(currentDate) == true) {
        estimatedEndDate
      } else {
        null
      }
    }
  }

  private fun getDeviceStateValue(data: ChannelDataBase) = when {
    data.isOnline().not() -> R.string.offline
    getChannelStateUseCase(data).isActive() -> R.string.details_timer_device_on
    else -> R.string.details_timer_device_off
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
            electricityMeterState = it.electricityMeterState?.copy(currentMonthDownloading = true)
          )
        }
      }

      else -> {
        with(currentState()) {
          loadData(remoteId, itemType, cleanupDownloading = true)
        }
      }
    }
  }

  fun ElectricityMeterGeneralStateHandler.updateState(
    state: ElectricityMeterState?,
    data: ChannelDataBase,
    measurements: ElectricityMeasurements?
  ): ElectricityMeterState? =
    (data as? ChannelDataEntity)?.let { updateState(state, it, measurements) }
}

sealed class SwitchGeneralViewEvent : ViewEvent

data class SwitchGeneralViewState(
  val remoteId: Int = 0,
  val itemType: ItemType = ItemType.CHANNEL,
  val initialDataLoadStarted: Boolean = false,
  val online: Boolean? = null,

  val deviceStateLabel: StringProvider = { "" },
  val deviceStateIcon: ImageId? = null,
  @StringRes val deviceStateValue: Int = R.string.offline,

  val showButtons: Boolean = true,
  val onIcon: ImageId? = null,
  val offIcon: ImageId? = null,

  val electricityMeterState: ElectricityMeterState? = null
) : ViewState()

private val SuplaChannelFunction.switchWithButtons: Boolean
  get() = when (this) {
    SuplaChannelFunction.POWER_SWITCH,
    SuplaChannelFunction.STAIRCASE_TIMER,
    SuplaChannelFunction.LIGHTSWITCH -> true

    else -> false
  }
