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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.events.OnlineEventsManager
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaChannelState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REFRESH_INTERVAL_MS = 4000
private const val TAG = "StateDialogViewModel"

@HiltViewModel
class StateDialogViewModel @Inject constructor(
  private val readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase,
  private val getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase,
  private val onlineEventsManager: OnlineEventsManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val dateProvider: DateProvider,
  roomProfileRepository: RoomProfileRepository,
  authorizeUseCase: AuthorizeUseCase,
  loginUseCase: LoginUseCase,
  val schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<StateDialogViewModelState, StateDialogViewEvent>(
  suplaClientProvider,
  roomProfileRepository,
  loginUseCase,
  authorizeUseCase,
  StateDialogViewModelState(),
  schedulers
),
  StateDialogScope {

  private var onlineDisposable: Disposable? = null
  private var refreshDisposable: Disposable? = null

  private var channels: List<ChannelData>? = null
  private var lightSourceLifespan: Int? = null
  private var lastRefreshTimestamp: Long? = null
  private var idx: Int = 0

  private val currentChannel: ChannelData?
    get() = channels?.getOrNull(idx)

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

  override fun onAuthorized(reason: AuthorizationReason) {
    closeAuthorizationDialog()
    if (reason is LifespanSettingsReason) {
      sendEvent(StateDialogViewEvent.ShowLifespanSettingsDialog(reason.remoteId, reason.caption, reason.lifespan))
    }
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
      onError = {}
    )
  }

  fun updateStateDialog(state: SuplaChannelState?) {
    val (channelState) = guardLet(state) { return }
    Trace.i(TAG, "Handling channel state for ${channelState.channelId}")

    if (currentChannel?.remoteId != channelState.channelId) {
      return
    }
    Trace.i(TAG, "Updating channel state for ${channelState.channelId}")

    updateState { viewState ->
      lightSourceLifespan = state?.lightSourceLifespan

      if (currentChannel?.online == true) {
        val values = StateDialogItem.entries.associateWith { it.extractor(channelState) }
          .filter { it.value != null }
          .mapValues { it.value!! }

        viewState.copy(viewState = viewState.viewState?.copy(loading = false, values = values))
      } else {
        viewState.copy(viewState = viewState.viewState?.copy(loading = false))
      }
    }
  }

  private fun showDialog(channels: List<ChannelData>) {
    if (channels.isEmpty()) {
      return
    }

    this.channels = channels
    this.idx = 0

    updateState {
      it.copy(
        viewState = StateDialogViewState(
          title = channels[0].caption,
          online = channels[0].online,
          subtitle = (channels.size > 1).ifTrue { localizedString(R.string.state_dialog_index, 1, channels.size) },
          loading = channels[0].online && channels[0].infoSupported,
          showArrows = channels.size > 1,
          showChangeLifespanButton = channels[0].showLifespanSettingsButton,
          function = channels[0].function,
          values = mapOf(
            StateDialogItem.CHANNEL_ID to LocalizedString.Constant("${channels[0].remoteId}")
          )
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
          values = currentChannel?.let {
            it.infoSupported.not().ifTrue {
              mapOf(StateDialogItem.CHANNEL_ID to LocalizedString.Constant("${it.remoteId}"))
            }
          } ?: state.viewState.values
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
                  Trace.d(TAG, "Asking for channel state $id")
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
      function = getChannelDefaultCaptionUseCase(function),
      caption = getCaptionUseCase(shareable),
      showLifespanSettingsButton = showLifespanSettingsButton,
      infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(flags),
      online = status.online
    )

  private val ChannelWithChildren.channels: List<ChannelData>
    get() = mutableListOf<ChannelData>()
      .apply {
        add(channel.channelData)
        addAll(allDescendantFlat.map { it.channelDataEntity.channelData })
      }
      .distinctBy { it.remoteId }
}

sealed class StateDialogViewEvent : ViewEvent {
  data class ShowLifespanSettingsDialog(
    val remoteId: Int,
    val caption: LocalizedString,
    val lightSourceLifespan: Int?
  ) : StateDialogViewEvent()
}

data class StateDialogViewModelState(
  val viewState: StateDialogViewState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()
