package org.supla.android.ui.dialogs.state
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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.thermostatdetail.slaves.ThermostatData
import org.supla.android.lib.SuplaChannelState
import java.util.concurrent.TimeUnit

private const val REFRESH_INTERVAL_MS = 4000

interface StateDialogViewModelState {
  var remoteId: Int?
  var lastRefreshTimestamp: Long?
  var disposable: Disposable?

  fun startRefreshing(dateProvider: DateProvider, suplaClientProvider: SuplaClientProvider) {
    remoteId?.let { id ->
      disposable =
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

  fun stopRefreshing() {
    disposable?.dispose()
  }

  fun cleanup() {
    remoteId = null
    lastRefreshTimestamp = null
    disposable?.dispose()
    disposable = null
  }
}

private data class StateDialogViewModelStateImpl(
  override var remoteId: Int? = null,
  override var lastRefreshTimestamp: Long? = null,
  override var disposable: Disposable? = null
) : StateDialogViewModelState

interface StateDialogHandler {
  val suplaClientProvider: SuplaClientProvider
  val dateProvider: DateProvider
  val stateDialogViewModelState: StateDialogViewModelState

  fun updateDialogState(updater: (StateDialogViewState?) -> StateDialogViewState?)

  fun default(): StateDialogViewModelState = StateDialogViewModelStateImpl()

  fun showStateDialog(thermostat: ThermostatData) {
    updateDialogState {
      StateDialogViewState(remoteId = thermostat.channelId, title = thermostat.caption)
    }

    suplaClientProvider.provide()?.getChannelState(thermostat.channelId)
    stateDialogViewModelState.remoteId = thermostat.channelId
    stateDialogViewModelState.lastRefreshTimestamp = dateProvider.currentTimestamp()
    stateDialogViewModelState.startRefreshing(dateProvider, suplaClientProvider)
  }

  fun closeStateDialog() {
    stateDialogViewModelState.cleanup()
    updateDialogState { null }
  }

  fun updateStateDialog(state: SuplaChannelState?) {
    val (channelState) = guardLet(state) { return }

    updateDialogState { viewState ->
      if (viewState?.remoteId == channelState.channelID) {
        val values = StateDialogItem.entries.associateWith { it.extractor(channelState) }
          .filter { it.value != null }
          .mapValues { it.value!! }

        viewState.copy(loading = false, values = values)
      } else {
        viewState
      }
    }
  }
}
