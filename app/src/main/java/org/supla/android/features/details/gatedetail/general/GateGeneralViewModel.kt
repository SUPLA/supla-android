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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntityConvertible
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class GateGeneralViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  private val readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  override val getChannelStateUseCase: GetChannelStateUseCase,
  override val getChannelIconUseCase: GetChannelIconUseCase,
  override val getCaptionUseCase: GetCaptionUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<GateGeneralModelState, GateGeneralViewEvent>(GateGeneralModelState(), schedulers),
  GateGeneralScope,
  ChannelGroupRelationDataEntityConvertible {

  fun observeData(remoteId: Int, type: ItemType) {
    when (type) {
      ItemType.CHANNEL -> observeChannel(remoteId)
      ItemType.GROUP -> observeGroup(remoteId)
    }
  }

  override fun onOpenClose() {
    triggerAction(ActionId.OPEN_CLOSE)
  }

  override fun onOpen() {
    triggerAction(ActionId.OPEN)
  }

  override fun onClose() {
    triggerAction(ActionId.CLOSE)
  }

  private fun observeChannel(remoteId: Int) {
    observeChannelWithChildrenUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handleChannel,
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channelWithChildren: ChannelWithChildren) {
    val channel = channelWithChildren.channel
    val channelState = getChannelStateUseCase(channel)
    val hasSensor = channelWithChildren.hasSensor

    updateState { state ->
      state.copy(
        remoteId = channel.remoteId,
        type = ItemType.CHANNEL,
        viewState = state.viewState.copy(
          offline = channel.status.offline,
          deviceStateData = DeviceStateData(
            icon = getChannelIconUseCase(channel),
            label = localizedString(R.string.details_timer_state_label),
            value = getDeviceStateValue(channel.status, channelState),
          ),
          openButtonState = hasSensor.ifTrue {
            SwitchButtonState(
              icon = getChannelIconUseCase(channel, channelStateValue = ChannelState.Value.OPEN),
              textRes = R.string.channel_btn_open,
              pressed = channelState.value == ChannelState.Value.OPEN
            )
          },
          closeButtonState = hasSensor.ifTrue {
            SwitchButtonState(
              icon = getChannelIconUseCase(channel, channelStateValue = ChannelState.Value.CLOSED),
              textRes = R.string.channel_btn_close,
              pressed = channelState.value == ChannelState.Value.CLOSED
            )
          }
        )
      )
    }
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

  private fun observeGroup(remoteId: Int) {
    readGroupWithChannelsUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handleGroup,
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleGroup(groupWithChannels: GroupWithChannels) {
    updateState { state ->
      state.copy(
        remoteId = groupWithChannels.group.remoteId,
        type = ItemType.GROUP,
        viewState = state.viewState.copy(
          offline = false,
          relatedChannelsData = groupWithChannels.channels.map { it.relatedChannelData }
        )
      )
    }
  }

  private fun triggerAction(actionId: ActionId) {
    val state = currentState()
    val remoteId = state.remoteId
    val type = state.type

    if (remoteId != null && type != null) {
      executeSimpleActionUseCase(actionId, type.subjectType, remoteId)
        .attachSilent()
        .subscribe()
        .disposeBySelf()
    }
  }
}

private val ChannelWithChildren.hasSensor: Boolean
  get() = children.firstOrNull { it.relationType == ChannelRelationType.OPENING_SENSOR } != null ||
    children.firstOrNull { it.relationType == ChannelRelationType.PARTIAL_OPENING_SENSOR } != null

sealed interface GateGeneralViewEvent : ViewEvent

data class GateGeneralModelState(
  val remoteId: Int? = null,
  val type: ItemType? = null,
  val viewState: GateGeneralViewState = GateGeneralViewState()
) : ViewState()
