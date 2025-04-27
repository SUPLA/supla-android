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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.monthStart
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterGeneralStateHandler
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterState
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterGeneralStateHandler
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterState
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements
import org.supla.android.usecases.channel.measurements.ImpulseCounterMeasurements
import org.supla.android.usecases.channel.measurements.SummarizedMeasurements
import org.supla.android.usecases.channel.measurements.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import org.supla.android.usecases.channel.measurements.impulsecounter.LoadImpulseCounterMeasurementsUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SwitchGeneralViewModel @Inject constructor(
  private val loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase,
  private val loadImpulseCounterMeasurementsUseCase: LoadImpulseCounterMeasurementsUseCase,
  private val electricityMeterGeneralStateHandler: ElectricityMeterGeneralStateHandler,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val impulseCounterGeneralStateHandler: ImpulseCounterGeneralStateHandler,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase,
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val dateProvider: DateProvider,
  private val preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseViewModel<SwitchGeneralViewState, SwitchGeneralViewEvent>(SwitchGeneralViewState(), schedulers) {

  fun onViewCreated(remoteId: Int) {
    observeDownload(remoteId)
  }

  fun loadData(remoteId: Int, itemType: ItemType, cleanupDownloading: Boolean = false) {
    getDataSource(remoteId, itemType)
      .flatMap { channelBase ->
        (channelBase as? ChannelWithChildren)?.isOrHasElectricityMeter?.ifTrue {
          loadElectricityMeterMeasurementsUseCase(remoteId, dateProvider.currentDate().monthStart())
            .map { Pair(channelBase, it) }
        }
          ?: (channelBase as? ChannelWithChildren)?.isOrHasImpulseCounter?.ifTrue {
            loadImpulseCounterMeasurementsUseCase(remoteId, dateProvider.currentDate().monthStart())
              .map { Pair(channelBase, it) }
          }
          ?: Maybe.just(Pair<ChannelDataBase, SummarizedMeasurements?>(channelBase, null))
      }
      .attachSilent()
      .subscribeBy(
        onSuccess = { (channelBase, measurements) -> handleData(channelBase, measurements, cleanupDownloading) },
        onError = defaultErrorHandler("loadData($remoteId, $itemType)")
      )
      .disposeBySelf()
  }

  private fun getDataSource(remoteId: Int, itemType: ItemType) = when (itemType) {
    ItemType.CHANNEL -> readChannelWithChildrenUseCase(remoteId)
    ItemType.GROUP -> readChannelGroupByRemoteIdUseCase(remoteId)
  }

  fun turnOn(remoteId: Int, itemType: ItemType) {
    if (currentState().flags.contains(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)) {
      updateState { it.copy(showOvercurrentDialog = true) }
    } else {
      performAction(ActionId.TURN_ON, itemType, remoteId)
    }
  }

  fun forceTurnOn(remoteId: Int, itemType: ItemType) {
    updateState { it.copy(showOvercurrentDialog = false) }
    performAction(ActionId.TURN_ON, itemType, remoteId)
  }

  fun turnOff(remoteId: Int, itemType: ItemType) {
    performAction(ActionId.TURN_OFF, itemType, remoteId)
  }

  fun onIntroductionClose() {
    preferences.setEmGeneralIntroductionShown()
    updateState { it.copy(electricityMeterState = it.electricityMeterState?.copy(showIntroduction = false)) }
  }

  fun hideOvercurrentDialog() {
    updateState { it.copy(showOvercurrentDialog = false) }
  }

  private fun performAction(actionId: ActionId, itemType: ItemType, remoteId: Int) {
    executeSimpleActionUseCase(actionId, itemType.subjectType, remoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performAction($actionId, $itemType, $remoteId)"))
      .disposeBySelf()
  }

  private fun handleData(data: ChannelDataBase, measurements: SummarizedMeasurements?, cleanupDownloading: Boolean) {
    updateState { state ->
      (data as? ChannelWithChildren)?.let {
        if ((data.isOrHasElectricityMeter || data.isOrHasImpulseCounter) && !state.initialDataLoadStarted) {
          downloadChannelMeasurementsUseCase.invoke(data)
        }
      }

      val downloading = when {
        cleanupDownloading -> false
        (data as? ChannelWithChildren)?.isOrHasElectricityMeter == true -> state.electricityMeterState?.currentMonthDownloading ?: false
        (data as? ChannelWithChildren)?.isOrHasImpulseCounter == true -> state.impulseCounterState?.currentMonthDownloading ?: false
        else -> false
      }
      val showButtons = data.function.switchWithButtons
      val flags = (data as? ChannelWithChildren)?.channel?.channelValueEntity?.asRelayValue()?.flags ?: emptyList()
      val issues = when (data) {
        is ChannelWithChildren -> getAllChannelIssuesUseCase(data.shareable)
        else -> null
      }

      state.copy(
        remoteId = data.remoteId,
        itemType = if (data is ChannelGroupDataEntity) ItemType.GROUP else ItemType.CHANNEL,
        online = data.status.online,
        flags = flags,
        initialDataLoadStarted = true,
        deviceStateLabel = getDeviceStateLabel(data),
        deviceStateIcon = getChannelIconUseCase(data),
        deviceStateValue = getDeviceStateValue(data),
        showButtons = showButtons,
        channelIssues = issues,
        onIcon = showButtons.ifTrue(getChannelIconUseCase(data, channelStateValue = ChannelState.Value.ON)),
        offIcon = showButtons.ifTrue(getChannelIconUseCase(data, channelStateValue = ChannelState.Value.OFF)),
        electricityMeterState = electricityMeterGeneralStateHandler
          .updateState(state.electricityMeterState, data, measurements)
          ?.copy(currentMonthDownloading = downloading),
        impulseCounterState = impulseCounterGeneralStateHandler
          .updateState(state.impulseCounterState, data, measurements)
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
    return (channelDataBase as? ChannelWithChildren)?.let {
      val currentDate = dateProvider.currentDate()
      val estimatedEndDate = it.channel.channelExtendedValueEntity?.getSuplaValue()?.timerEstimatedEndDate

      if (estimatedEndDate?.after(currentDate) == true) {
        estimatedEndDate
      } else {
        null
      }
    }
  }

  private fun getDeviceStateValue(data: ChannelDataBase) = when {
    data.status.offline -> R.string.offline
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
            electricityMeterState = it.electricityMeterState?.copy(currentMonthDownloading = true),
            impulseCounterState = it.impulseCounterState?.copy(currentMonthDownloading = true)
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
    measurements: SummarizedMeasurements?
  ): ElectricityMeterState? =
    (data as? ChannelWithChildren)?.let { updateState(state, it, measurements as? ElectricityMeasurements) }

  fun ImpulseCounterGeneralStateHandler.updateState(
    state: ImpulseCounterState?,
    data: ChannelDataBase,
    measurements: SummarizedMeasurements?
  ): ImpulseCounterState? =
    (data as? ChannelWithChildren)?.let { updateState(state, it, measurements as? ImpulseCounterMeasurements) }
}

sealed class SwitchGeneralViewEvent : ViewEvent

data class SwitchGeneralViewState(
  val remoteId: Int = 0,
  val itemType: ItemType = ItemType.CHANNEL,
  val initialDataLoadStarted: Boolean = false,
  val online: Boolean? = null,
  val flags: List<SuplaRelayFlag> = emptyList(),

  val deviceStateLabel: StringProvider = { "" },
  val deviceStateIcon: ImageId? = null,
  @StringRes val deviceStateValue: Int = R.string.offline,

  val showButtons: Boolean = true,
  val onIcon: ImageId? = null,
  val offIcon: ImageId? = null,
  val channelIssues: List<ChannelIssueItem>? = null,

  val showOvercurrentDialog: Boolean = false,
  val electricityMeterState: ElectricityMeterState? = null,
  val impulseCounterState: ImpulseCounterState? = null
) : ViewState() {
  val leftButtonState: SwitchButtonState
    get() = SwitchButtonState(
      icon = offIcon,
      textRes = R.string.channel_btn_off,
      pressed = deviceStateValue == R.string.details_timer_device_off
    )

  val rightButtonState: SwitchButtonState
    get() = SwitchButtonState(
      icon = onIcon,
      textRes = R.string.channel_btn_on,
      pressed = deviceStateValue == R.string.details_timer_device_on
    )
}

private val SuplaFunction.switchWithButtons: Boolean
  get() = when (this) {
    SuplaFunction.POWER_SWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.LIGHTSWITCH -> true

    else -> false
  }
