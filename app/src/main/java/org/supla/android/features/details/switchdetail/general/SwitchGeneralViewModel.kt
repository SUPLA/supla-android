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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterGeneralStateHandler
import org.supla.android.features.details.detailbase.electricitymeter.ElectricityMeterState
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterGeneralStateHandler
import org.supla.android.features.details.detailbase.impulsecounter.ImpulseCounterState
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.ui.views.DeviceStateData
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
import org.supla.android.usecases.group.ChannelGroupRelationDataEntityConvertible
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.relay.RelayMode
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.usecase.GetCaptionUseCase
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
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val dateProvider: DateProvider,
  private val preferences: ApplicationPreferences,
  override val getChannelStateUseCase: GetChannelStateUseCase,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  threading: SuplaThreading
) : BaseViewModel<SwitchGeneralViewState, SwitchGeneralViewEvent>(SwitchGeneralViewState(), threading),
  SwitchGeneralScope,
  ChannelGroupRelationDataEntityConvertible {

  private var remoteId = 0
  private var itemType = ItemType.CHANNEL

  init {
    setupSuplaClientMessageHandler(suplaClientMessageHandlerWrapper)
  }

  fun onViewCreated(remoteId: Int) {
    observeDownload(remoteId)
  }

  override fun handleSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelDataChanged)?.let {
      if (it.channelId == remoteId && itemType == ItemType.CHANNEL) {
        loadData(remoteId, itemType)
      }
    }
    (message as? SuplaClientMessage.GroupDataChanged)?.let {
      if (it.groupId == remoteId && itemType == ItemType.GROUP) {
        loadData(remoteId, itemType)
      }
    }
  }

  fun loadData(remoteId: Int, itemType: ItemType, cleanupDownloading: Boolean = false) {
    this.remoteId = remoteId
    this.itemType = itemType

    when (itemType) {
      ItemType.CHANNEL -> loadChannel(remoteId, cleanupDownloading)
      ItemType.GROUP -> loadGroup(remoteId)
    }
  }

  fun forceTurnOn(remoteId: Int, itemType: ItemType) {
    updateState { it.copy(showOvercurrentDialog = false) }
    performAction(ActionId.TURN_ON, itemType, remoteId)
  }

  override fun onTurnOn() {
    val state = currentState()
    if (state.flags.contains(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)) {
      updateState { it.copy(showOvercurrentDialog = true) }
    } else {
      performAction(ActionId.TURN_ON, state.itemType, state.remoteId)
    }
  }

  override fun onTurnOff() {
    val state = currentState()
    performAction(ActionId.TURN_OFF, state.itemType, state.remoteId)
  }

  override fun onIntroductionClose() {
    preferences.setEmGeneralIntroductionShown()
    updateState { it.copy(electricityMeterState = it.electricityMeterState?.copy(showIntroduction = false)) }
  }

  override fun onForce() {
  }

  override fun onManual() {
  }

  override fun onWeekly() {
  }

  override fun onAuto() {
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

  private fun loadChannel(remoteId: Int, cleanupDownloading: Boolean) {
    readChannelWithChildrenUseCase(remoteId)
      .flatMap { channelWithChildren ->
        channelWithChildren.isOrHasElectricityMeter.forTrue {
          loadElectricityMeterMeasurementsUseCase(
            profileId = channelWithChildren.profileId,
            remoteId = remoteId,
            startTimestamp = dateProvider.currentDate().monthStart().time
          )
            .map { Pair(channelWithChildren, it) }
        }
          ?: channelWithChildren.isOrHasImpulseCounter.forTrue {
            loadImpulseCounterMeasurementsUseCase(remoteId, dateProvider.currentDate().monthStart())
              .map { Pair(channelWithChildren, it) }
          }
          ?: Maybe.just(Pair<ChannelWithChildren, SummarizedMeasurements?>(channelWithChildren, null))
      }
      .attachSilent()
      .subscribeBy(
        onSuccess = { (channelBase, measurements) -> handleChannel(channelBase, measurements, cleanupDownloading) },
        onError = defaultErrorHandler("loadChannel($remoteId, $cleanupDownloading)")
      )
      .disposeBySelf()
  }

  private fun handleChannel(data: ChannelWithChildren, measurements: SummarizedMeasurements?, cleanupDownloading: Boolean) {
    updateState { state ->
      data.let {
        if ((data.isOrHasElectricityMeter || data.isOrHasImpulseCounter) && !state.initialDataLoadStarted) {
          downloadChannelMeasurementsUseCase.invoke(data)
        }
      }

      val downloading = when {
        cleanupDownloading -> false
        data.isOrHasElectricityMeter -> state.electricityMeterState?.currentMonthDownloading ?: false
        data.isOrHasImpulseCounter -> state.impulseCounterState?.currentMonthDownloading ?: false
        else -> false
      }
      val showButtons = data.function.switchWithButtons
      val channelState = getChannelStateUseCase(data)
      val value = data.channel.channelValueEntity.asRelayValue()

      state.copy(
        remoteId = data.remoteId,
        itemType = ItemType.CHANNEL,
        leftButtonDisabled = data.status.offline,
        rightButtonDisabled = data.status.offline,
        flags = value.flags,
        initialDataLoadStarted = true,
        deviceStateData = DeviceStateData(
          label = getDeviceStateLabel(data),
          icon = getChannelIconUseCase(data),
          value = getDeviceStateValue(data)
        ),
        channelIssues = getAllChannelIssuesUseCase(data.shareable),
        leftButtonState = showButtons.forTrue {
          SwitchButtonState(
            icon = getChannelIconUseCase(data, channelStateValue = ChannelState.Value.OFF),
            textRes = R.string.channel_btn_off,
            pressed = channelState.value == ChannelState.Value.OFF
          )
        },
        rightButtonState = showButtons.forTrue {
          SwitchButtonState(
            icon = getChannelIconUseCase(data, channelStateValue = ChannelState.Value.ON),
            textRes = R.string.channel_btn_on,
            pressed = channelState.value == ChannelState.Value.ON
          )
        },
        electricityMeterState = electricityMeterGeneralStateHandler
          .updateState(state.electricityMeterState, data, measurements)
          ?.copy(currentMonthDownloading = downloading),
        impulseCounterState = impulseCounterGeneralStateHandler
          .updateState(state.impulseCounterState, data, measurements)
          ?.copy(currentMonthDownloading = downloading),
        forceSupported = data.channel.forceSupported,
        forceActive = data.channel.forceActive,
        operatingMode = OperatingMode(channelFlags = data.channel.flags, relayValue = value),
        scale = preferences.scale
      )
    }
  }

  private fun getDeviceStateLabel(data: ChannelDataBase): LocalizedString {
    return getEstimatedCountDownEndTime(data)?.let { date ->
      LocalizedString.WithResourceAndDate(R.string.details_timer_state_label_for_timer, date.time)
    } ?: localizedString(R.string.details_timer_state_label)
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
    data.status.offline -> localizedString(R.string.offline)
    getChannelStateUseCase(data).isActive -> localizedString(R.string.details_timer_device_on)
    else -> localizedString(R.string.details_timer_device_off)
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

  private fun loadGroup(remoteId: Int) {
    readGroupWithChannelsUseCase(remoteId)
      .firstElement()
      .attachSilent()
      .subscribeBy(
        onSuccess = this::handleGroup,
        onError = defaultErrorHandler("loadGroup($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleGroup(groupWithChannels: GroupWithChannels) {
    val groupState: ChannelState.Value? = groupWithChannels.aggregatedState(GroupWithChannels.Policy.OnOff)

    updateState { state ->
      state.copy(
        remoteId = groupWithChannels.group.remoteId,
        itemType = ItemType.GROUP,
        leftButtonDisabled = groupWithChannels.group.status.offline,
        rightButtonDisabled = groupWithChannels.group.status.offline,
        flags = emptyList(),
        initialDataLoadStarted = true,
        deviceStateData = null,
        channelIssues = emptyList(),
        leftButtonState = SwitchButtonState(
          icon = getChannelIconUseCase(groupWithChannels.group, channelStateValue = ChannelState.Value.OFF),
          textRes = R.string.channel_btn_off,
          pressed = groupState == ChannelState.Value.OFF
        ),
        rightButtonState = SwitchButtonState(
          icon = getChannelIconUseCase(groupWithChannels.group, channelStateValue = ChannelState.Value.ON),
          textRes = R.string.channel_btn_on,
          pressed = groupState == ChannelState.Value.ON
        ),
        electricityMeterState = null,
        impulseCounterState = null,
        relatedChannelsData = groupWithChannels.relatedChannelData,
        scale = preferences.scale
      )
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
  val flags: List<SuplaRelayFlag> = emptyList(),

  val deviceStateData: DeviceStateData? = null,
  val channelIssues: List<ChannelIssueItem>? = null,

  val showOvercurrentDialog: Boolean = false,
  val electricityMeterState: ElectricityMeterState? = null,
  val impulseCounterState: ImpulseCounterState? = null,
  val relatedChannelsData: List<RelatedChannelData>? = null,
  val leftButtonState: SwitchButtonState? = null,
  val rightButtonState: SwitchButtonState? = null,

  // Schedule extension
  val leftButtonDisabled: Boolean = false,
  val rightButtonDisabled: Boolean = false,
  val forceSupported: Boolean = false,
  val forceActive: Boolean = false,
  val operatingMode: OperatingMode? = null,

  val scale: Float = 1f
) : ViewState()

@JvmInline
value class OperatingMode(val value: Int) {
  val manualAllowed: Boolean
    get() = value and MASK_MANUAL_ALLOWED == MASK_MANUAL_ALLOWED
  val weeklyAllowed: Boolean
    get() = value and MASK_WEEKLY_ALLOWED == MASK_WEEKLY_ALLOWED
  val autoAllowed: Boolean
    get() = value and MASK_AUTO_ALLOWED == MASK_AUTO_ALLOWED

  val manualActive: Boolean
    get() = value and MASK_MANUAL_ACTIVE == MASK_MANUAL_ACTIVE
  val weeklyActive: Boolean
    get() = value and MASK_WEEKLY_ACTIVE == MASK_WEEKLY_ACTIVE
  val autoActive: Boolean
    get() = value and MASK_AUTO_ACTIVE == MASK_AUTO_ACTIVE

  companion object {
    private const val MASK_MANUAL_ALLOWED = 0x01
    private const val MASK_WEEKLY_ALLOWED = 0x02
    private const val MASK_AUTO_ALLOWED = 0x04
    private const val MASK_MANUAL_ACTIVE = 0x08
    private const val MASK_WEEKLY_ACTIVE = 0x10
    private const val MASK_AUTO_ACTIVE = 0x20

    operator fun invoke(channelFlags: Long, relayValue: RelayValue): OperatingMode? {
      val weeklyAllowed = SuplaChannelFlag.WEEKLY_SCHEDULE inside channelFlags
      val autoAllowed = SuplaChannelFlag.RELAY_MODE_AUTOMATIC_SUPPORTED inside channelFlags

      if (!weeklyAllowed && !autoAllowed) {
        return null
      }

      val weeklyActive = relayValue.flags.contains(SuplaRelayFlag.WEEKLY_SCHEDULE_ENABLED)
      val autoActive = relayValue.mode == RelayMode.AUTOMATIC && !weeklyActive

      return OperatingMode(
        value =
        MASK_MANUAL_ALLOWED or
          (if (weeklyAllowed) MASK_WEEKLY_ALLOWED else 0) or
          (if (autoAllowed) MASK_AUTO_ALLOWED else 0) or
          (if (!weeklyActive && !autoActive) MASK_MANUAL_ACTIVE else 0) or
          (if (weeklyActive) MASK_WEEKLY_ACTIVE else 0) or
          (if (autoActive) MASK_AUTO_ACTIVE else 0)
      )
    }

    operator fun invoke(
      manualAllowed: Boolean = false,
      weeklyAllowed: Boolean = false,
      autoAllowed: Boolean = false,
      manualActive: Boolean = false,
      weeklyActive: Boolean = false,
      autoActive: Boolean = false
    ) = OperatingMode(
      value =
      (if (manualAllowed) MASK_MANUAL_ALLOWED else 0) or
        (if (weeklyAllowed) MASK_WEEKLY_ALLOWED else 0) or
        (if (autoAllowed) MASK_AUTO_ALLOWED else 0) or
        (if (manualActive) MASK_MANUAL_ACTIVE else 0) or
        (if (weeklyActive) MASK_WEEKLY_ACTIVE else 0) or
        (if (autoActive) MASK_AUTO_ACTIVE else 0)
    )
  }
}

private val SuplaFunction.switchWithButtons: Boolean
  get() = when (this) {
    SuplaFunction.POWER_SWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.LIGHTSWITCH -> true
    else -> false
  }

private val ChannelDataEntity.forceSupported: Boolean
  get() = SuplaChannelFlag.RELAY_MODE_FORCED_SUPPORTED inside flags

private val ChannelDataEntity.forceActive: Boolean
  get() = forceSupported &&
    (channelValueEntity.asRelayValue().mode == RelayMode.FORCED_ON || channelValueEntity.asRelayValue().mode == RelayMode.FORCED_OFF)
