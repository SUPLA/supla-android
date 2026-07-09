package org.supla.android.features.statedialog
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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.events.OnlineEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.toEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModelScope
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REFRESH_INTERVAL_MS = 4000

@HiltViewModel
class StateDialogViewModel @Inject constructor(
  private val readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase,
  private val getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase,
  private val onlineEventsManager: OnlineEventsManager,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val dateProvider: DateProvider,
  override val suplaClientProvider: SuplaClientProvider,
  override val profileRepository: ProfileRepository,
  override val authorizeUseCase: AuthorizeUseCase,
  override val loginUseCase: LoginUseCase,
  override val schedulers: SuplaSchedulers,
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper
) : BaseViewModel<StateDialogViewModelState, StateDialogViewEvent>(
  defaultState = StateDialogViewModelState(),
  schedulers = schedulers
),
  StateDialogScope,
  LifespanDialogScope,
  BaseAuthorizationViewModelScope {

  private var onlineDisposable: Disposable? = null
  private var refreshDisposable: Disposable? = null

  private var channels: List<ChannelData>? = null
  private var lightSourceLifespan: Int? = null
  private var lastRefreshTimestamp: Long? = null
  private var idx: Int = 0

  private val currentChannel: ChannelData?
    get() = channels?.getOrNull(idx)

  init {
    setupSuplaClientMessageHandler(suplaClientMessageHandlerWrapper)
  }

  override fun handleSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelState)?.let { updateStateDialog(it.channelState) }
  }

  override fun onDismiss() {
    channels = null
    lightSourceLifespan = null
    idx = 0
    lastRefreshTimestamp = null
    refreshDisposable?.dispose()
    refreshDisposable = null
    onlineDisposable?.dispose()
    onlineDisposable = null

    updateState { it.copy(viewState = null) }
  }

  override fun onNext() {
    onChannelChange {
      idx = idx.plus(1).mod(channels?.size ?: 1)
    }
  }

  override fun onPrevious() {
    onChannelChange {
      val newIdx = idx - 1
      idx = if (newIdx < 0) {
        channels?.size?.minus(1) ?: 0
      } else {
        newIdx
      }
    }
  }

  override fun onChangeLifespan() {
    currentChannel?.let { channel ->

      val reason = LifespanSettingsReason(
        remoteId = channel.remoteId,
        caption = channel.caption,
        lifespan = lightSourceLifespan
      )
      onDismiss()
      showAuthorizationDialog(reason = reason)
    }
  }

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) =
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }

  override fun getAuthorizationDialogState(): AuthorizationDialogState? =
    currentState().authorizationDialogState

  override fun onAuthorized(reason: AuthorizationReason) {
    closeAuthorizationDialog()
    if (reason is LifespanSettingsReason) {
      updateState {
        val lifespan = reason.lifespan ?: 0
        it.copy(
          lifespanDialogViewState = LifespanDialogState(
            title = reason.caption,
            lifespanValue = "$lifespan",
            saveEnabled = lifespan > 0,
            lifespanInitialValue = lifespan,
            channelId = reason.remoteId
          )
        )
      }
    }
  }

  override fun launch(launcher: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch { launcher() }
  }

  override fun onStart() {
    currentChannel?.remoteId?.let {
      startRefreshing(it)
    }
  }

  override fun onStop() {
    stopRefreshing()
  }

  fun showDialog(channelRemoteId: Int) {
    subscribe(
      readChannelWithChildrenTreeUseCase(channelRemoteId).firstElement(),
      onSuccess = { showDialog(it.channels) },
      onError = defaultErrorHandler("showDialog($channelRemoteId)")
    )
  }

  fun updateStateDialog(state: SuplaChannelState?) {
    val (channelState) = guardLet(state) { return }
    Timber.i("Handling channel state for ${channelState.channelId}")

    if (currentChannel?.remoteId != channelState.channelId) {
      return
    }
    Timber.i("Updating channel state for ${channelState.channelId}")
    currentChannel?.let { it.lastKnownState = state?.toEntity(it.profileId) }

    updateState { viewState ->
      lightSourceLifespan = state?.lightSourceLifespan

      viewState.copy(
        viewState = viewState.viewState?.copy(
          loading = false,
          values = StateDialogItem.values(channelState)
        )
      )
    }
  }

  private fun showDialog(channels: List<ChannelData>) {
    if (channels.isEmpty()) {
      return
    }

    this.channels = channels
    this.idx = 0

    updateState { state ->
      state.copy(
        viewState = StateDialogViewState(
          title = channels[0].caption,
          online = channels[0].online,
          subtitle = (channels.size > 1).forTrue { localizedString(R.string.state_dialog_index, 1, channels.size) },
          loading = channels[0].online && channels[0].infoSupported,
          showArrows = channels.size > 1,
          showChangeLifespanButton = channels[0].showLifespanSettingsButton,
          function = channels[0].function,
          values = channels[0].lastKnownState?.let {
            StateDialogItem.values(it)
          } ?: mapOf(StateDialogItem.CHANNEL_ID to LocalizedString.Constant("${channels[0].remoteId}"))
        )
      )
    }

    onlineDisposable = onlineEventsManager
      .observe()
      .subscribeBy(
        onNext = { onlineData ->
          channels.firstOrNull { it.remoteId == onlineData.remoteId }?.online = onlineData.online
          if (channels.getOrNull(idx)?.remoteId == onlineData.remoteId) {
            updateState {
              it.copy(viewState = it.viewState?.copy(online = onlineData.online))
            }
          }
        }
      )

    startRefreshing(channels[0].remoteId)
  }

  private fun onChannelChange(changeOperation: () -> Unit) {
    stopRefreshing()

    changeOperation()

    val idxToDisplay = idx + 1
    val count = channels?.size ?: 1
    val loading = currentChannel?.let { it.online && it.infoSupported } ?: false

    updateState { state ->
      state.copy(
        viewState = state.viewState?.copy(
          title = currentChannel?.caption ?: LocalizedString.Empty,
          subtitle = localizedString(R.string.state_dialog_index, idxToDisplay, count),
          loading = loading,
          online = currentChannel?.online ?: false,
          function = currentChannel?.function,
          values = currentChannel?.lastKnownState?.let { StateDialogItem.values(it) } ?: emptyMap()
        )
      )
    }

    currentChannel?.remoteId?.let { startRefreshing(it) }
  }

  private fun startRefreshing(remoteId: Int) {
    suplaClientProvider.provide()?.getChannelState(remoteId)
    lastRefreshTimestamp = dateProvider.currentTimestamp()

    currentChannel?.remoteId?.let { id ->
      refreshDisposable =
        Observable.interval(100, TimeUnit.MILLISECONDS)
          .subscribeBy(
            onNext = {
              lastRefreshTimestamp?.let {
                val currentTimestamp = dateProvider.currentTimestamp()
                if (it + REFRESH_INTERVAL_MS < currentTimestamp) {
                  Timber.d("Asking for channel state $id")
                  lastRefreshTimestamp = currentTimestamp
                  suplaClientProvider.provide()?.getChannelState(id)
                }
              }
            }
          )
    }
  }

  private fun stopRefreshing() {
    refreshDisposable?.dispose()
  }

  private val ChannelDataEntity.showLifespanSettingsButton: Boolean
    get() = SuplaChannelFlag.LIGHT_SOURCE_LIFESPAN_SETTABLE inside flags

  private val ChannelDataEntity.channelData: ChannelData
    get() = ChannelData(
      remoteId = remoteId,
      profileId = profileId,
      function = getChannelDefaultCaptionUseCase(function),
      caption = getCaptionUseCase(shareable),
      showLifespanSettingsButton = showLifespanSettingsButton,
      infoSupported = showInfo,
      lastKnownState = stateEntity,
      online = status.online
    )

  private val ChannelWithChildren.channels: List<ChannelData>
    get() = mutableListOf<ChannelData>()
      .apply {
        add(channel.channelData)
        addAll(allDescendantFlat.map { it.channelDataEntity.channelData })
      }
      .distinctBy { it.remoteId }

  override fun onLifespanDialogDismiss() {
    updateState { it.copy(lifespanDialogViewState = null) }
  }

  override fun onLifeSpanDialogResetChange(checked: Boolean) {
    updateState {
      it.copy(
        lifespanDialogViewState = it.lifespanDialogViewState?.copy(resetActive = checked)
      )
    }
  }

  override fun onLifespanDialogValueChange(value: String) {
    updateState {
      it.copy(
        lifespanDialogViewState = it.lifespanDialogViewState?.copy(
          lifespanValue = value,
          saveEnabled = value.isNotEmpty() && value.toIntOrNull() != null
        )
      )
    }
  }

  override fun onLifespanDialogOk() {
    updateState {
      it.copy(lifespanDialogViewState = it.lifespanDialogViewState?.copy(processing = true))
    }

    Completable.fromRunnable {
      val suplaClient = suplaClientProvider.provide() ?: throw IllegalStateException()
      val state = currentState().lifespanDialogViewState ?: throw IllegalStateException()
      val lifespan = state.lifespanValue.toIntOrNull() ?: throw IllegalStateException()

      val result = suplaClient.setLightsourceLifespan(
        remoteId = state.channelId,
        resetCounter = state.resetActive,
        setTime = lifespan != state.lifespanInitialValue,
        lifespan = lifespan
      )

      if (!result) {
        throw IllegalStateException()
      }
    }
      .attach()
      .subscribeBy(
        onComplete = {
          updateState { it.copy(lifespanDialogViewState = null) }
        },
        onError = {
          updateState {
            it.copy(lifespanDialogViewState = it.lifespanDialogViewState?.copy(error = true, processing = false))
          }
        }
      )
      .disposeBySelf()
  }
}

sealed class StateDialogViewEvent : ViewEvent

data class StateDialogViewModelState(
  val viewState: StateDialogViewState? = null,
  val lifespanDialogViewState: LifespanDialogState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()
