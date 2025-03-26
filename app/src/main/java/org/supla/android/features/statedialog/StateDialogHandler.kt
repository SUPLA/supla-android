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

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.events.OnlineEventsManager
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaChannelState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.usecases.channel.ReadChannelWithChildrenTreeUseCase
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase
import java.util.concurrent.TimeUnit

private const val REFRESH_INTERVAL_MS = 4000
private const val TAG = "StateDialogHandler"

interface StateDialogHandler : StateDialogScope {
  val readChannelWithChildrenTreeUseCase: ReadChannelWithChildrenTreeUseCase
  val getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase
  val stateDialogViewModelState: StateDialogViewModelState
  val onlineEventsManager: OnlineEventsManager
  val suplaClientProvider: SuplaClientProvider
  val getCaptionUseCase: GetCaptionUseCase
  val dateProvider: DateProvider

  fun <T : Any> subscribe(maybe: Maybe<T>, onSuccess: (T) -> Unit = {}, onError: (Throwable) -> Unit = {})

  fun updateDialogState(updater: (StateDialogViewState?) -> StateDialogViewState?)

  fun showAuthorizationDialog(reason: AuthorizationReason)

  fun default(): StateDialogViewModelState = StateDialogViewModelStateImpl()

  fun onStart() {
    stateDialogViewModelState.startRefreshing(dateProvider, suplaClientProvider)
  }

  fun onStop() {
    stateDialogViewModelState.stopRefreshing()
  }

  fun showStateDialog(channelRemoteId: Int) {
    subscribe(
      readChannelWithChildrenTreeUseCase(channelRemoteId).firstElement(),
      onSuccess = { showStateDialog(it.channels) },
      onError = {}
    )
  }

  private fun showStateDialog(channels: List<ChannelData>) {
    if (channels.isEmpty()) {
      return
    }

    updateDialogState {
      StateDialogViewState(
        title = channels[0].caption,
        online = channels[0].online,
        subtitle = (channels.size > 1).ifTrue { LocalizedString.WithResourceIntInt(R.string.state_dialog_index, 1, channels.size) },
        showArrows = channels.size > 1,
        showChangeLifespanButton = channels[0].showLifespanSettingsButton,
        function = channels[0].function
      )
    }

    stateDialogViewModelState.channels = channels
    stateDialogViewModelState.idx = 0
    startRefreshing(channels[0].remoteId)

    stateDialogViewModelState.onlineDisposable = onlineEventsManager.observe()
      .subscribeBy(
        onNext = { onlineData ->
          stateDialogViewModelState.channels?.firstOrNull { it.remoteId == onlineData.remoteId }?.online = onlineData.online
          if (stateDialogViewModelState.remoteId == onlineData.remoteId) {
            updateDialogState {
              it?.copy(online = onlineData.online)
            }
          }
        }
      )
  }

  fun updateStateDialog(state: SuplaChannelState?) {
    val (channelState) = guardLet(state) { return }
    Trace.i(TAG, "Handling channel state for ${channelState.channelId}")

    if (stateDialogViewModelState.remoteId != channelState.channelId) {
      return
    }
    Trace.i(TAG, "Updating channel state for ${channelState.channelId}")

    updateDialogState { viewState ->
      stateDialogViewModelState.lightSourceLifespan = state?.lightSourceLifespan

      if (stateDialogViewModelState.online) {
        val values = StateDialogItem.entries.associateWith { it.extractor(channelState) }
          .filter { it.value != null }
          .mapValues { it.value!! }

        viewState?.copy(loading = false, values = values)
      } else {
        viewState?.copy(loading = false)
      }
    }
  }

  override fun onStateDialogDismiss() {
    stateDialogViewModelState.cleanup()
    updateDialogState { null }
  }

  override fun onStateDialogNext() {
    onChannelChange {
      stateDialogViewModelState.idx = stateDialogViewModelState.idx.plus(1).mod(stateDialogViewModelState.channels?.size ?: 1)
    }
  }

  override fun onStateDialogPrevious() {
    onChannelChange {
      val newIdx = stateDialogViewModelState.idx - 1
      if (newIdx < 0) {
        stateDialogViewModelState.idx = stateDialogViewModelState.channels?.size?.minus(1) ?: 0
      } else {
        stateDialogViewModelState.idx = newIdx
      }
    }
  }

  override fun onStateDialogChangeLifespan() {
    stateDialogViewModelState.channels?.let { channels ->
      onStateDialogDismiss()

      val remoteId = channels[stateDialogViewModelState.idx].remoteId
      val caption = channels[stateDialogViewModelState.idx].caption

      val reason = LifespanSettingsReason(
        remoteId = remoteId,
        caption = caption,
        lifespan = stateDialogViewModelState.lightSourceLifespan
      )
      showAuthorizationDialog(reason = reason)
    }
  }

  private fun onChannelChange(changeOperation: () -> Unit) {
    stateDialogViewModelState.stopRefreshing()

    changeOperation()

    val idx = stateDialogViewModelState.idx + 1
    val count = stateDialogViewModelState.channels?.size ?: 1

    updateDialogState { state ->
      state?.copy(
        title = stateDialogViewModelState.title ?: LocalizedString.Empty,
        subtitle = LocalizedString.WithResourceIntInt(R.string.state_dialog_index, idx, count),
        loading = true,
        online = stateDialogViewModelState.online,
        function = stateDialogViewModelState.function
      )
    }

    stateDialogViewModelState.remoteId?.let {
      startRefreshing(it)
    }
  }

  private fun startRefreshing(remoteId: Int) {
    suplaClientProvider.provide()?.getChannelState(remoteId)
    stateDialogViewModelState.lastRefreshTimestamp = dateProvider.currentTimestamp()
    stateDialogViewModelState.startRefreshing(dateProvider, suplaClientProvider)
  }

  private val ChannelDataEntity.showLifespanSettingsButton: Boolean
    get() = SuplaChannelFlag.LIGHT_SOURCE_LIFESPAN_SETTABLE inside flags

  private val ChannelDataEntity.channelData: ChannelData
    get() = ChannelData(
      remoteId = remoteId,
      function = getChannelDefaultCaptionUseCase(function),
      caption = getCaptionUseCase(shareable),
      showLifespanSettingsButton = showLifespanSettingsButton,
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

interface StateDialogViewModelState {
  var channels: List<ChannelData>?
  var lightSourceLifespan: Int?
  var lastRefreshTimestamp: Long?
  var refreshDisposable: Disposable?
  var onlineDisposable: Disposable?
  var idx: Int

  val remoteId: Int?
    get() = channels?.getOrNull(idx)?.remoteId

  val title: LocalizedString?
    get() = channels?.getOrNull(idx)?.caption

  val online: Boolean
    get() = channels?.getOrNull(idx)?.online ?: false

  val function: LocalizedString?
    get() = channels?.getOrNull(idx)?.function
}

private data class StateDialogViewModelStateImpl(
  override var channels: List<ChannelData>? = null,
  override var lightSourceLifespan: Int? = null,
  override var lastRefreshTimestamp: Long? = null,
  override var refreshDisposable: Disposable? = null,
  override var onlineDisposable: Disposable? = null,
  override var idx: Int = 0
) : StateDialogViewModelState

private fun StateDialogViewModelState.startRefreshing(dateProvider: DateProvider, suplaClientProvider: SuplaClientProvider) {
  channels?.getOrNull(idx)?.remoteId?.let { id ->
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

fun StateDialogViewModelState.stopRefreshing() {
  refreshDisposable?.dispose()
}

fun StateDialogViewModelState.cleanup() {
  channels = null
  lightSourceLifespan = null
  idx = 0
  lastRefreshTimestamp = null
  refreshDisposable?.dispose()
  refreshDisposable = null
  onlineDisposable?.dispose()
  onlineDisposable = null
}
