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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ProfileRepository
import org.supla.android.extensions.subscribeBy
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModelScope
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val disconnectUseCase: DisconnectUseCase,
  override val suplaClientProvider: SuplaClientProvider,
  override val profileRepository: ProfileRepository,
  override val loginUseCase: LoginUseCase,
  override val authorizeUseCase: AuthorizeUseCase,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<StatusViewState, StatusViewEvent>(
  StatusViewState(),
  suplaSchedulers
),
  StatusViewScope,
  BaseAuthorizationViewModelScope {

  private var stateDisposable: Disposable? = null

  override fun onStart() {
    stateDisposable = suplaClientStateHolder.state()
      .attachSilent()
      .subscribeBy(
        onNext = { state ->
          when (state) {
            SuplaClientState.Connected -> sendEvent(StatusViewEvent.NavigateToMain)
            is SuplaClientState.Finished -> handleErrorState(state)
            SuplaClientState.Initialization ->
              updateState {
                it.copy(
                  viewType = StatusViewState.ViewType.CONNECTING,
                  stateText = StatusViewStateText.INITIALIZING
                )
              }
            is SuplaClientState.Connecting ->
              updateState {
                it.copy(
                  viewType = StatusViewState.ViewType.CONNECTING,
                  stateText = if (state.reason == SuplaClientState.Reason.NoNetwork) {
                    StatusViewStateText.AWAITING_NETWORK
                  } else {
                    StatusViewStateText.CONNECTING
                  }
                )
              }
            is SuplaClientState.Disconnecting,
            SuplaClientState.Locking ->
              updateState {
                it.copy(
                  viewType = StatusViewState.ViewType.CONNECTING,
                  stateText = StatusViewStateText.DISCONNECTING
                )
              }
            else -> {}
          }
        }
      )
  }

  override fun onStop() {
    stateDisposable?.dispose()
  }

  override fun onCancelAndGoToProfilesClick() {
    disconnectAndOpenProfiles()
  }

  override fun onTryAgain() {
    suplaClientStateHolder.handleEvent(SuplaClientEvent.Initialized)
  }

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun getAuthorizationDialogState(): AuthorizationDialogState? =
    currentState().authorizationDialogState

  override fun onAuthorized(reason: AuthorizationReason) {
    updateState {
      it.copy(authorizationDialogState = null)
    }
  }

  override fun launch(launcher: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch { launcher() }
  }

  override fun onAuthorize(userName: String, password: String) {
    login(userName, password)
  }

  private fun handleErrorState(state: SuplaClientState.Finished) {
    if (state.reason is SuplaClientState.Reason.RegisterError && state.reason.shouldAuthorize()) {
      showAuthorizationDialog()
    }

    updateState {
      it.copy(
        viewType = StatusViewState.ViewType.ERROR,
        errorDescription = state.reason?.let { error -> getErrorDescription(error) }
      )
    }
  }

  private fun getErrorDescription(reason: SuplaClientState.Reason): LocalizedString? {
    return when (reason) {
      is SuplaClientState.Reason.ConnectionError ->
        if (reason.error.Code == SUPLA_RESULT_HOST_NOT_FOUND) {
          localizedString(R.string.err_hostnotfound)
        } else {
          null
        }
      is SuplaClientState.Reason.RegisterError -> reason.error.codeToString(true)
      SuplaClientState.Reason.NoNetwork,
      SuplaClientState.Reason.VersionError,
      SuplaClientState.Reason.AppInBackground,
      SuplaClientState.Reason.AddWizardStarted -> null
    }
  }

  private fun disconnectAndOpenProfiles() {
    disconnectUseCase()
      .attachSilent()
      .subscribeBy(
        onComplete = { sendEvent(StatusViewEvent.NavigateToProfiles) },
        onError = {
          Timber.i("Disconnecting broken.")

          if (it !is InterruptedException) {
            Timber.e(it, "Could not disconnect Supla client!")
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

data class StatusViewState(
  val stateText: StatusViewStateText = StatusViewStateText.INITIALIZING,
  val errorDescription: LocalizedString? = null,
  val viewType: ViewType = ViewType.CONNECTING,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState() {
  enum class ViewType {
    CONNECTING, ERROR
  }
}
