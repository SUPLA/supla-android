package org.supla.android.features.details.gatedetail.general
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
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.hasPositionSensor
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.features.details.relayschedule.BaseRelayGeneralViewModel
import org.supla.android.features.details.relayschedule.OperatingMode
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteRelayActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.ChannelGroupRelationDataEntityConvertible
import org.supla.android.usecases.group.ChannelInGroup
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class GateGeneralViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  override val getChannelStateUseCase: GetChannelStateUseCase,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  private val preferences: ApplicationPreferences,
  readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  executeRelayActionUseCase: ExecuteRelayActionUseCase,
  channelConfigEventsManager: ChannelConfigEventsManager,
  deviceConfigEventsManager: DeviceConfigEventsManager,
  suplaClientProvider: SuplaClientProvider,
  dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseRelayGeneralViewModel<GateGeneralViewState, GateGeneralViewEvent>(
  GateGeneralViewState(),
  readGroupWithChannelsUseCase,
  executeSimpleActionUseCase,
  executeRelayActionUseCase,
  channelConfigEventsManager,
  deviceConfigEventsManager,
  suplaClientProvider,
  dateProvider,
  threading
),
  GateGeneralScope,
  ChannelGroupRelationDataEntityConvertible {

  override fun onOpenClose() {
    performAction(ActionId.OPEN_CLOSE)
  }

  override fun onOpen() {
    performAction(ActionId.OPEN)
  }

  override fun onClose() {
    performAction(ActionId.CLOSE)
  }

  override fun onForce() {
    val state = currentState()
    if (state.forceActive) {
      performAction(forceDeactivationAction(state.operatingMode?.weeklyActive == true))
      return
    }

    performRelayAction(SuplaRelayMode.FORCED_OFF)
  }

  override fun observeChannel(remoteId: Int, deviceId: Int) {
    observeChannel(observeChannelWithChildrenUseCase(remoteId), remoteId, deviceId, ::buildChannelState)
  }

  private fun buildChannelState(
    channelWithChildren: ChannelWithChildren,
    schedule: RelayScheduleContext
  ): GateGeneralViewState {
    val state = currentState()
    val channel = channelWithChildren.channel
    val channelState = getChannelStateUseCase(channel)
    val relayValue = channel.channelValueEntity.asRelayValue()
    val weeklyScheduleEnabled = relayValue.weeklyScheduleEnabled
    val actionButtonsDisabled = channel.status.offline ||
      (weeklyScheduleEnabled && relayValue.mode == SuplaRelayMode.FORCED_OFF)
    val showOpenAndClose = channelWithChildren.hasPositionSensor && channelWithChildren.function.supportsOpenAndClose

    return state.copy(
      offline = channel.status.offline,
      manualButtonDisabled = channel.status.offline,
      weeklyButtonDisabled = channel.status.offline,
      autoButtonDisabled = channel.status.offline,
      forceButtonDisabled = weeklyScheduleEnabled,
      actionButtonsDisabled = actionButtonsDisabled,
      deviceStateData = DeviceStateData(
        icon = getChannelIconUseCase(channel),
        label = localizedString(R.string.details_timer_state_label),
        value = getDeviceStateValue(channel.status, channelState),
      ),
      mainButtonLabel = mainButtonLabel(channel.function),
      openButtonState = showOpenAndClose.forTrue {
        SwitchButtonState(
          icon = getChannelIconUseCase(channel, channelStateValue = ChannelState.Value.OPEN),
          textRes = R.string.channel_btn_open,
          pressed = channelState.value == ChannelState.Value.OPEN
        )
      },
      closeButtonState = showOpenAndClose.forTrue {
        SwitchButtonState(
          icon = getChannelIconUseCase(channel, channelStateValue = ChannelState.Value.CLOSED),
          textRes = R.string.channel_btn_close,
          pressed = channelState.value == ChannelState.Value.CLOSED
        )
      },
      programInfo = schedule.programInfo(channel.status.offline, relayValue),
      operatingMode = OperatingMode(channelFlags = channel.flags, relayValue = relayValue),
      forceSupported = channel.forceSupported,
      forceActive = channel.forceActive(relayValue),
      lockIconType = lockIconType(relayValue),
      scale = preferences.scale
    )
  }

  private fun getDeviceStateValue(status: SuplaChannelAvailabilityStatus, state: ChannelState): LocalizedString {
    if (status.offline) {
      return localizedString(R.string.offline)
    }

    return when (state.value) {
      ChannelState.Value.OPEN -> localizedString(R.string.state_opened)
      ChannelState.Value.PARTIALLY_OPENED -> localizedString(R.string.state_partially_opened)
      ChannelState.Value.CLOSED -> localizedString(R.string.state_closed)
      else -> LocalizedString.Empty
    }
  }

  override fun handleGroup(groupWithChannels: GroupWithChannels) {
    val groupOffline = groupWithChannels.group.status.offline
    val showOpenAndClose = groupWithChannels.channels.firstOrNull { it.hasSensor && it.function.supportsOpenAndClose } != null
    val gateWithoutSensor = groupWithChannels.channels.firstOrNull { !it.hasSensor } != null
    val groupState: ChannelState.Value? = groupWithChannels.aggregatedState(GroupWithChannels.Policy.OpenClosed)

    updateState { state ->
      state.copy(
        offline = groupOffline,
        manualButtonDisabled = groupOffline,
        weeklyButtonDisabled = groupOffline,
        autoButtonDisabled = groupOffline,
        forceButtonDisabled = false,
        actionButtonsDisabled = groupOffline,
        mainButtonLabel = mainButtonLabel(groupWithChannels.group.function),
        relatedChannelsData = groupWithChannels.relatedChannelData,
        openButtonState = showOpenAndClose.forTrue {
          SwitchButtonState(
            icon = getChannelIconUseCase(groupWithChannels.group, channelStateValue = ChannelState.Value.OPEN),
            textRes = R.string.channel_btn_open,
            pressed = groupState == ChannelState.Value.OPEN
          )
        },
        closeButtonState = showOpenAndClose.forTrue {
          SwitchButtonState(
            icon = getChannelIconUseCase(groupWithChannels.group, channelStateValue = ChannelState.Value.CLOSED),
            textRes = R.string.channel_btn_close,
            pressed = groupState == ChannelState.Value.CLOSED
          )
        },
        showOpenAndCloseWarning = groupWithChannels.group.function.supportsOpenAndClose && showOpenAndClose && gateWithoutSensor,
        programInfo = emptyList(),
        operatingMode = null,
        forceSupported = false,
        forceActive = false,
        scale = preferences.scale
      )
    }
  }

  private fun mainButtonLabel(function: SuplaFunction): LocalizedString =
    when (function) {
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK -> localizedString(R.string.channel_btn_open)
      else -> localizedString(R.string.channel_btn_step_by_step)
    }
}

private val ChannelInGroup.hasSensor: Boolean
  get() = when (this) {
    ChannelInGroup.Invisible -> false
    is ChannelInGroup.Visible -> channelWithChildren.hasPositionSensor
  }

private val SuplaFunction.supportsOpenAndClose: Boolean
  get() = when (this) {
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.CONTROLLING_THE_GATE -> true
    else -> false
  }

sealed interface GateGeneralViewEvent : ViewEvent
