package org.supla.android.features.details.containerdetail.general
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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.container.SuplaChannelContainerConfig
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelUpdatesObserver
import org.supla.android.events.UpdateEventsManager
import org.supla.android.features.details.containerdetail.general.ui.ContainerType
import org.supla.android.features.details.containerdetail.general.ui.ControlLevel
import org.supla.android.features.details.containerdetail.general.ui.ErrorLevel
import org.supla.android.features.details.containerdetail.general.ui.WarningLevel
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.CaptionChangeDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.ui.dialogs.state.StateDialogHandler
import org.supla.android.ui.dialogs.state.StateDialogViewModelState
import org.supla.android.ui.dialogs.state.StateDialogViewState
import org.supla.android.ui.lists.sensordata.SensorItemData
import org.supla.android.usecases.captionchange.CaptionChangeHandler
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.SuplaClientOperation
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.function.container.ContainerFlag
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import javax.inject.Inject

@HiltViewModel
class ContainerGeneralDetailViewModel @Inject constructor(
  private val callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
  private val getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val valuesFormatter: ValuesFormatter,
  private val preferences: Preferences,
  override val captionChangeUseCase: CaptionChangeUseCase,
  override val suplaClientProvider: SuplaClientProvider,
  override val updateEventsManager: UpdateEventsManager,
  override val vibrationHelper: VibrationHelper,
  override val schedulers: SuplaSchedulers,
  override val dateProvider: DateProvider,
  profileRepository: RoomProfileRepository,
  authorizeUseCase: AuthorizeUseCase,
  loginUseCase: LoginUseCase
) : BaseAuthorizationViewModel<ContainerGeneralDetailViewModeState, ContainerGeneralDetailViewEvent>(
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  ContainerGeneralDetailViewModeState(),
  schedulers
),
  ContainerGeneralDetailViewScope,
  StateDialogHandler,
  ChannelUpdatesObserver,
  CaptionChangeHandler {

  override val stateDialogViewModelState: StateDialogViewModelState = default()

  override val captionChangeDialogState: CaptionChangeDialogState?
    get() = currentState().captionChangeDialogState

  override fun updateDialogState(updater: (StateDialogViewState?) -> StateDialogViewState?) {
    updateState { it.copy(stateDialogViewState = updater(it.stateDialogViewState)) }
  }

  override fun updateCaptionChangeDialogState(updater: (CaptionChangeDialogState?) -> CaptionChangeDialogState?) {
    updateState { it.copy(captionChangeDialogState = updater(it.captionChangeDialogState)) }
  }

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorizationCancel() {
    updateCaptionChangeDialogState { null }
    super.onAuthorizationCancel()
  }

  override fun onAuthorizationDismiss() {
    updateCaptionChangeDialogState { null }
    super.onAuthorizationDismiss()
  }

  override fun onAuthorized(reason: AuthorizationReason) {
    if (reason is MuteSound) {
      closeAuthorizationDialog()
      muteAlarmSound()
    } else {
      updateState {
        it.copy(
          authorizationDialogState = null,
          captionChangeDialogState = it.captionChangeDialogState?.copy(authorized = true)
        )
      }
    }
  }

  override fun onInfoClick(data: SensorItemData) {
    showStateDialog(data.channelId, data.caption)
  }

  override fun onCaptionLongPress(data: SensorItemData) {
    changeChannelCaption(data.userCaption, data.channelId, data.profileId)
  }

  override fun onMuteClick() {
    vibrationHelper.vibrate()
    if (currentState().muteAuthorizationNeeded) {
      showAuthorizationDialog(reason = MuteSound)
    } else {
      muteAlarmSound()
    }
  }

  override fun onChannelUpdate(channelWithChildren: ChannelWithChildren) {
    loadData(channelWithChildren.remoteId)
  }

  fun loadData(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId),
      loadChannelConfigUseCase(remoteId).toMaybe(),
    ) { channel, config -> Pair(channel, config) }
      .attach()
      .subscribeBy(
        onSuccess = this::handle,
        onError = defaultErrorHandler("loadData()")
      )
      .disposeBySelf()
  }

  private fun handle(data: Pair<ChannelWithChildren, SuplaChannelConfig>) {
    val channelWithChildren = data.first
    val config = data.second as? SuplaChannelContainerConfig
    val channelToLevelMap = config?.sensors?.associate { Pair(it.channelId, it.fillLevel) }
    val value = channelWithChildren.channel.channelValueEntity.asContainerValue()
    val level = value.levelKnown.ifTrue { value.level.div(100f) }
    val levelString = when {
      value.status.offline -> "offline"
      level == null -> "---"
      else -> valuesFormatter.getPercentageString(level)
    }
    updateState { state ->
      state.copy(
        remoteId = channelWithChildren.channel.remoteId,
        muteAuthorizationNeeded = config?.muteAlarmSoundWithoutAdditionalAuth?.not() ?: false,
        viewState = state.viewState.copy(
          fluidLevel = level,
          fluidLevelString = levelString,
          containerType = channelWithChildren.channel.function.containerType,
          scale = preferences.scale,
          controlLevels = config?.let { createControlLevels(it) } ?: emptyList(),
          sensors = channelWithChildren.children
            .filter { it.relationType == ChannelRelationType.DEFAULT }
            .map { it.toSensorData(channelToLevelMap) },
          issues = getAllChannelIssuesUseCase(channelWithChildren.shareable),
          soundOn = value.status.online && value.flags.contains(ContainerFlag.SOUND_ALARM_ON)
        )
      )
    }
  }

  private fun ChannelChildEntity.toSensorData(channelToLevelMap: Map<Int, Int>?): SensorItemData {
    val caption = getCaptionUseCase(channelDataEntity.shareable)
    val captionWithPercentage = channelToLevelMap?.get(channel.remoteId)?.let {
      LocalizedString.WithResourceStringInt(R.string.container_caption, caption, it)
    }

    return SensorItemData(
      channelId = channel.remoteId,
      profileId = channel.profileId,
      onlineState = channelDataEntity.channelValueEntity.onlineState,
      icon = getChannelIconUseCase(channelDataEntity),
      caption = captionWithPercentage ?: caption,
      userCaption = channel.caption,
      batteryIcon = getChannelBatteryIconUseCase(channelDataEntity.shareable),
      showChannelStateIcon = channelDataEntity.channelValueEntity.status.online && SuplaChannelFlag.CHANNEL_STATE inside channel.flags
    )
  }

  private fun createControlLevels(config: SuplaChannelContainerConfig): List<ControlLevel> {
    val result = mutableListOf<ControlLevel>()

    if (config.alarmAboveLevel > 0) {
      result.add(errorLevel(config.alarmAboveLevel, ControlLevel.Type.UPPER))
    }
    if (config.warningAboveLevel > 0) {
      result.add(warningLevel(config.warningAboveLevel, ControlLevel.Type.UPPER))
    }
    if (config.warningBelowLevel > 0) {
      result.add(warningLevel(config.warningBelowLevel, ControlLevel.Type.LOWER))
    }
    if (config.alarmBelowLevel > 0) {
      result.add(errorLevel(config.alarmBelowLevel, ControlLevel.Type.LOWER))
    }

    return result.sortedByDescending { it.level }
  }

  private fun errorLevel(level: Int, type: ControlLevel.Type) =
    level.minus(1).div(100f).let {
      ErrorLevel(it, valuesFormatter.getPercentageString(it), type)
    }

  private fun warningLevel(level: Int, type: ControlLevel.Type) =
    level.minus(1).div(100f).let {
      WarningLevel(it, valuesFormatter.getPercentageString(it), type)
    }

  private fun muteAlarmSound() {
    callSuplaClientOperationUseCase(currentState().remoteId, ItemType.CHANNEL, SuplaClientOperation.Command.MuteAlarmSound)
      .attachSilent()
      .subscribeBy(
        onError = defaultErrorHandler("onAuthorized")
      )
      .disposeBySelf()
  }
}

sealed class ContainerGeneralDetailViewEvent : ViewEvent

data class ContainerGeneralDetailViewModeState(
  val remoteId: Int = 0,
  val muteAuthorizationNeeded: Boolean = false,
  val viewState: ContainerGeneralDetailViewState = ContainerGeneralDetailViewState(),
  val stateDialogViewState: StateDialogViewState? = null,
  val captionChangeDialogState: CaptionChangeDialogState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()

private data object MuteSound : AuthorizationReason

private val SuplaFunction.containerType: ContainerType
  get() = when (this) {
    SuplaFunction.WATER_TANK -> ContainerType.WATER
    SuplaFunction.SEPTIC_TANK -> ContainerType.SEPTIC
    else -> ContainerType.DEFAULT
  }
