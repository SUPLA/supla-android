package org.supla.android.features.details.thermostatdetail.slaves
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
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.ValuesFormatter.Companion.NO_VALUE_TEXT
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.thermostat.getIndicatorIcon
import org.supla.android.data.source.remote.thermostat.getSetpointText
import org.supla.android.features.statedialog.StateDialogHandler
import org.supla.android.features.statedialog.StateDialogViewModelState
import org.supla.android.features.statedialog.StateDialogViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.CaptionChangeDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.captionchange.CaptionChangeHandler
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForSlavesUseCase
import javax.inject.Inject

@HiltViewModel
class ThermostatSlavesListViewModel @Inject constructor(
  private val getChannelIssuesForSlavesUseCase: GetChannelIssuesForSlavesUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val valuesFormatter: ValuesFormatter,
  private val preferences: Preferences,
  override val readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase,
  override val captionChangeUseCase: CaptionChangeUseCase,
  override val suplaClientProvider: SuplaClientProvider,
  override val getCaptionUseCase: GetCaptionUseCase,
  override val vibrationHelper: VibrationHelper,
  override val dateProvider: DateProvider,
  roomProfileRepository: RoomProfileRepository,
  authorizeUseCase: AuthorizeUseCase,
  schedulers: SuplaSchedulers,
  loginUseCase: LoginUseCase
) : BaseAuthorizationViewModel<ThermostatSlavesListViewModelState, ThermostatSlavesListViewEvent>(
  suplaClientProvider,
  roomProfileRepository,
  loginUseCase,
  authorizeUseCase,
  ThermostatSlavesListViewModelState(),
  schedulers
),
  StateDialogHandler,
  CaptionChangeHandler {

  override val stateDialogViewModelState: StateDialogViewModelState = default()
  override val captionChangeDialogState: CaptionChangeDialogState?
    get() = currentState().captionChangeDialogState

  override fun updateCaptionChangeDialogState(updater: (CaptionChangeDialogState?) -> CaptionChangeDialogState?) {
    updateState { it.copy(captionChangeDialogState = updater(it.captionChangeDialogState)) }
  }

  override fun updateDialogState(updater: (StateDialogViewState?) -> StateDialogViewState?) {
    updateState { it.copy(stateDialogViewState = updater(it.stateDialogViewState)) }
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
    updateState {
      it.copy(
        authorizationDialogState = null,
        captionChangeDialogState = it.captionChangeDialogState?.copy(authorized = true)
      )
    }
  }

  fun onCreate(remoteId: Int) {
    readChannelWithChildrenTreeUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handle,
        onError = defaultErrorHandler("onCreate($remoteId)")
      )
      .disposeBySelf()
  }

  fun showMessage(message: String) {
    updateState { it.copy(showMessage = message) }
  }

  fun closeMessage() {
    updateState { it.copy(showMessage = null) }
  }

  private fun handle(channelWithChildren: ChannelWithChildren) {
    val slaves = channelWithChildren.allDescendantFlat
      .filter { it.relationType == ChannelRelationType.MASTER_THERMOSTAT }
      .map { it.toThermostatData(withSetpointValue = true) }

    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          master = channelWithChildren.toThermostatData(withSetpointValue = true),
          slaves = slaves,
          scale = preferences.scale
        )
      )
    }
  }

  private fun ChannelWithChildren.toThermostatData(withSetpointValue: Boolean = false): ThermostatData {
    val thermostatValue = channel.channelValueEntity.asThermostatValue()
    val mainThermometer = children.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }
    return ThermostatData(
      channelId = channel.remoteId,
      profileId = channel.profileId,
      onlineState = channel.channelValueEntity.onlineState,
      caption = getCaptionUseCase(channel.shareable),
      userCaption = channel.caption,
      imageId = getChannelIconUseCase(channel),
      currentPower = thermostatValue.state.power,
      value = mainThermometer?.let { getChannelValueStringUseCase(it.withChildren) } ?: NO_VALUE_TEXT,
      indicatorIcon = thermostatValue.getIndicatorIcon(),
      channelIssueItem = getChannelIssuesForSlavesUseCase(shareable),
      showChannelStateIcon = SuplaChannelFlag.CHANNEL_STATE inside channel.flags,
      subValue = withSetpointValue.ifTrue { thermostatValue.getSetpointText(valuesFormatter) },
      pumpSwitchIcon = pumpSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) },
      sourceSwitchIcon = heatOrColdSourceSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) }
    )
  }

  private fun ChannelChildEntity.toThermostatData(withSetpointValue: Boolean = false): ThermostatData {
    val thermostatValue = channelDataEntity.channelValueEntity.asThermostatValue()
    val mainThermometer = children.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }
    return ThermostatData(
      channelId = channel.remoteId,
      profileId = channel.profileId,
      onlineState = channelDataEntity.channelValueEntity.onlineState,
      caption = getCaptionUseCase(channelDataEntity.shareable),
      userCaption = channel.caption,
      imageId = getChannelIconUseCase(channelDataEntity),
      currentPower = thermostatValue.state.power,
      value = mainThermometer?.let { getChannelValueStringUseCase(it.withChildren) } ?: NO_VALUE_TEXT,
      indicatorIcon = thermostatValue.getIndicatorIcon(),
      channelIssueItem = getChannelIssuesForSlavesUseCase(channelDataEntity.shareable),
      showChannelStateIcon = SuplaChannelFlag.CHANNEL_STATE inside channel.flags,
      subValue = withSetpointValue.ifTrue { thermostatValue.getSetpointText(valuesFormatter) },
      pumpSwitchIcon = pumpSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) },
      sourceSwitchIcon = heatOrColdSourceSwitchChild?.let { getChannelIconUseCase(it.channelDataEntity) }
    )
  }
}

sealed class ThermostatSlavesListViewEvent : ViewEvent

data class ThermostatSlavesListViewModelState(
  val viewState: ThermostatSlavesListViewState = ThermostatSlavesListViewState(),
  val stateDialogViewState: StateDialogViewState? = null,
  val showMessage: String? = null,
  val captionChangeDialogState: CaptionChangeDialogState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()
