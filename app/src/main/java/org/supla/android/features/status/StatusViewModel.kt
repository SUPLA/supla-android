package org.supla.android.features.status
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
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.usecases.client.LoginUseCase
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val disconnectUseCase: DisconnectUseCase,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  suplaSchedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<StatusViewModelState, StatusViewEvent>(
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  StatusViewModelState(),
  suplaSchedulers
) {

  fun onStart() {
    suplaClientStateHolder.state()
      .attachSilent()
      .subscribeBy(
        onNext = { state ->
          when (state) {
            SuplaClientState.Connected -> sendEvent(StatusViewEvent.NavigateToMain)
            is SuplaClientState.Finished -> handleErrorState(state)

            SuplaClientState.Initialization ->
              updateState {
                it.copy(
                  viewType = StatusViewModelState.ViewType.CONNECTING,
                  viewState = it.viewState.copy(stateText = StatusViewStateText.INITIALIZING)
                )
              }

            is SuplaClientState.Connecting ->
              updateState {
                it.copy(
                  viewType = StatusViewModelState.ViewType.CONNECTING,
                  viewState = it.viewState.copy(
                    stateText = if (state.reason == SuplaClientState.Reason.NoNetwork) {
                      StatusViewStateText.AWAITING_NETWORK
                    } else {
                      StatusViewStateText.CONNECTING
                    }
                  )
                )
              }

            SuplaClientState.Disconnecting,
            SuplaClientState.Locking ->
              updateState {
                it.copy(
                  viewType = StatusViewModelState.ViewType.CONNECTING,
                  viewState = it.viewState.copy(stateText = StatusViewStateText.DISCONNECTING)
                )
              }

            else -> {}
          }
        }
      )
      .disposeBySelf()
  }

  fun cancelAndOpenProfiles() {
    disconnectAndOpenProfiles()
  }

  fun tryAgainClick() {
    suplaClientStateHolder.handleEvent(SuplaClientEvent.Initialized)
  }

  override fun updateDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized() {
    updateState {
      it.copy(authorizationDialogState = null)
    }
  }

  private fun handleErrorState(state: SuplaClientState.Finished) {
    if (state.reason is SuplaClientState.Reason.RegisterError && state.reason.shouldAuthorize()) {
      showAuthorizationDialog()
    }

    updateState {
      it.copy(
        viewType = StatusViewModelState.ViewType.ERROR,
        viewState = it.viewState.copy(
          errorDescription = state.reason?.let { error -> getErrorDescription(error) }
        )
      )
    }
  }

  private fun getErrorDescription(reason: SuplaClientState.Reason): StringProvider? {
    return when (reason) {
      is SuplaClientState.Reason.ConnectionError ->
        if (reason.error.Code == SUPLA_RESULT_HOST_NOT_FOUND) {
          { context -> context.getString(R.string.err_hostnotfound) }
        } else {
          null
        }

      is SuplaClientState.Reason.RegisterError -> { context -> reason.error.codeToString(context, true) }
      SuplaClientState.Reason.NoNetwork,
      SuplaClientState.Reason.VersionError,
      SuplaClientState.Reason.AppInBackground -> null
    }
  }

  private fun disconnectAndOpenProfiles() {
    disconnectUseCase()
      .attachSilent()
      .subscribeBy(
        onComplete = { sendEvent(StatusViewEvent.NavigateToProfiles) },
        onError = {
          Trace.i(TAG, "Disconnecting broken.")

          if (it !is InterruptedException) {
            Trace.e(TAG, "Could not disconnect Supla client!", it)
          }
        }
      )
      .disposeBySelf()
  }
}

sealed class StatusViewEvent : ViewEvent {
  data object NavigateToMain : StatusViewEvent()
  data object NavigateToProfiles : StatusViewEvent()
}

data class StatusViewModelState(
  val viewState: StatusViewState = StatusViewState(),
  val viewType: ViewType = ViewType.CONNECTING,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState() {
  enum class ViewType {
    CONNECTING, ERROR
  }
}
