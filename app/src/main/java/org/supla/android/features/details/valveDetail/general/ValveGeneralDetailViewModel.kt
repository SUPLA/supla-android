package org.supla.android.features.details.valveDetail.general
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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.events.ChannelUpdatesObserver
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.sensordata.SensorItemData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.usecases.channel.ActionException
import org.supla.android.usecases.channel.ButtonType
import org.supla.android.usecases.channel.ChannelActionUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.valve.ValveValue
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import javax.inject.Inject

@HiltViewModel
class ValveGeneralDetailViewModel @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val channelActionUseCase: ChannelActionUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val vibrationHelper: VibrationHelper,
  private val preferences: Preferences,
  override val updateEventsManager: UpdateEventsManager,
  override val schedulers: SuplaSchedulers
) : BaseViewModel<ValveGeneralDetailViewModeState, ValveGeneralDetailViewEvent>(
  ValveGeneralDetailViewModeState(),
  schedulers
),
  ChannelUpdatesObserver {

  override fun onChannelUpdate(channelWithChildren: ChannelWithChildren) {
    handle(channelWithChildren)
  }

  fun loadData(remoteId: Int) {
    readChannelWithChildrenUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = this::handle,
        onError = defaultErrorHandler("loadData()")
      )
      .disposeBySelf()
  }

  fun onActionClick(remoteId: Int, action: ValveAction) {
    vibrationHelper.vibrate()
    channelActionUseCase(remoteId, action.buttonType)
      .attach()
      .subscribeBy(
        onError = { error ->
          when (error) {
            is ActionException.ValveFloodingAlarm ->
              updateState { it.copy(dialog = ValveAlertDialog.Confirmation(R.string.valve_warning_flooding, ActionId.OPEN)) }

            is ActionException.ValveClosedManually ->
              updateState { it.copy(dialog = ValveAlertDialog.Confirmation(R.string.valve_warning_manually_closed, ActionId.OPEN)) }

            is ActionException.ValveMotorProblemOpening ->
              updateState { it.copy(dialog = ValveAlertDialog.Confirmation(R.string.valve_warning_motor_problem_opening, ActionId.OPEN)) }

            is ActionException.ValveMotorProblemClosing ->
              updateState { it.copy(dialog = ValveAlertDialog.Confirmation(R.string.valve_warning_motor_problem_closing, ActionId.CLOSE)) }

            else -> updateState { it.copy(dialog = ValveAlertDialog.Failure) }
          }
        }
      )
      .disposeBySelf()
  }

  fun forceAction(remoteId: Int, action: ActionId?) {
    if (action != null) {
      updateState { it.copy(dialog = null) }
      executeSimpleActionUseCase(action, SubjectType.CHANNEL, remoteId)
        .attachSilent()
        .subscribe()
        .disposeBySelf()
    }
  }

  fun closeErrorDialog() {
    updateState { it.copy(dialog = null) }
  }

  private fun handle(channelWithChildren: ChannelWithChildren) {
    val value = channelWithChildren.channel.channelValueEntity.asValveValue()
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          icon = getChannelIconUseCase(channelWithChildren.channel),
          stateStringRes = value.getStateStringRes(),
          issues = getAllChannelIssuesUseCase(channelWithChildren.shareable),
          sensors = channelWithChildren.children
            .filter { it.relationType == ChannelRelationType.DEFAULT }
            .map { it.toSensor() },
          offline = value.status.offline,
          scale = preferences.scale,
          leftButtonState = SwitchButtonState(
            getChannelIconUseCase(channelWithChildren, channelStateValue = ChannelState.Value.CLOSED),
            textRes = R.string.channel_btn_close,
            pressed = value.status.online && value.isClosed()
          ),
          rightButtonState = SwitchButtonState(
            getChannelIconUseCase(channelWithChildren, channelStateValue = ChannelState.Value.OPEN),
            textRes = R.string.channel_btn_open,
            pressed = value.status.online && value.isClosed().not()
          )
        )
      )
    }
  }

  private fun ChannelChildEntity.toSensor() =
    SensorItemData(
      channelId = channel.remoteId,
      profileId = channel.profileId,
      onlineState = channelDataEntity.channelValueEntity.onlineState,
      icon = getChannelIconUseCase(channelDataEntity),
      caption = getCaptionUseCase(channelDataEntity.shareable),
      userCaption = channel.caption,
      batteryIcon = getChannelBatteryIconUseCase(channelDataEntity.shareable),
      showChannelStateIcon = channelDataEntity.channelValueEntity.status.online && SuplaChannelFlag.CHANNEL_STATE inside channel.flags
    )

  private fun ValveValue.getStateStringRes(): Int =
    when {
      status.offline -> R.string.offline
      isClosed() -> R.string.state_closed
      else -> R.string.state_opened
    }
}

sealed class ValveGeneralDetailViewEvent : ViewEvent

data class ValveGeneralDetailViewModeState(
  val viewState: ValveGeneralDetailViewState = ValveGeneralDetailViewState(),
  val dialog: ValveAlertDialog? = null
) : ViewState()

sealed interface ValveAlertDialog {
  val messageRes: Int
  val positiveButtonRes: Int?
  val negativeButtonRes: Int?
  val action: ActionId?

  data class Confirmation(override val messageRes: Int, override val action: ActionId) : ValveAlertDialog {
    override val positiveButtonRes: Int = R.string.yes
    override val negativeButtonRes: Int = R.string.no
  }

  data object Failure : ValveAlertDialog {
    override val messageRes: Int = R.string.valve_action_error
    override val positiveButtonRes: Int? = null
    override val negativeButtonRes: Int = R.string.ok
    override val action: ActionId? = null
  }
}

enum class ValveAction(val buttonType: ButtonType) {
  OPEN(ButtonType.RIGHT), CLOSE(ButtonType.LEFT)
}
