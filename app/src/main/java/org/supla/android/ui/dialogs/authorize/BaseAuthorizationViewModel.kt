package org.supla.android.ui.dialogs.authorize
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

import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogScope
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.usecases.client.AuthorizationException
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

abstract class BaseAuthorizationViewModel<S : AuthorizationModelState, E : ViewEvent>(
  private val suplaClientProvider: SuplaClientProvider,
  private val profileRepository: RoomProfileRepository,
  private val loginUseCase: LoginUseCase,
  private val authorizeUseCase: AuthorizeUseCase,
  defaultState: S,
  schedulers: SuplaSchedulers,
) : BaseViewModel<S, E>(defaultState, schedulers), AuthorizationDialogScope {

  protected abstract fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?)

  abstract fun onAuthorized(reason: AuthorizationReason)

  override fun onAuthorizationDismiss() {
    closeAuthorizationDialog()
  }

  override fun onAuthorizationCancel() {
    closeAuthorizationDialog()
  }

  override fun onAuthorize(userName: String, password: String) {
    authorize(userName, password)
  }

  open fun onError(error: Throwable) {
    defaultErrorHandler("authorize")(error)
  }

  override fun onStateChange(state: AuthorizationDialogState) {
    updateAuthorizationDialogState { state }
  }

  fun showAuthorizationDialog(
    reason: AuthorizationReason = AuthorizationReason.Default,
    clarificationMessage: LocalizedString? = null
  ) {
    if (isAuthorized()) {
      onAuthorized(reason)
      return
    }

    profileRepository.findActiveProfile()
      .attach()
      .subscribeBy(
        onSuccess = { profile ->
          updateAuthorizationDialogState {
            it?.copy(
              userName = profile.email ?: "",
              isCloudAccount = profile.isCloudAccount,
              userNameEnabled = suplaClientProvider.provide()?.registered() == true,
              reason = reason,
              clarification = clarificationMessage
            ) ?: AuthorizationDialogState(
              userName = profile.email ?: "",
              isCloudAccount = profile.isCloudAccount,
              userNameEnabled = suplaClientProvider.provide()?.registered() == true,
              reason = reason,
              clarification = clarificationMessage
            )
          }
        }
      )
      .disposeBySelf()
  }

  fun authorize(userName: String, password: String) {
    authorizeUseCase(userName, password)
      .attachSilent()
      .doOnSubscribe { updateAuthorizationDialogState { it?.copy(processing = true) } }
      .doOnTerminate { updateAuthorizationDialogState { it?.copy(processing = false) } }
      .subscribeBy(
        onSuccess = {
          if (it.isAuthorized()) {
            onAuthorized(currentState().authorizationDialogState?.reason ?: AuthorizationReason.Default)
          } else {
            updateAuthorizationDialogState { state -> state?.copy(error = localizedString(R.string.status_unknown_err)) }
          }
        },
        onError = { error ->
          if (error is AuthorizationException) {
            updateAuthorizationDialogState { state ->
              state?.copy(error = error.localizedErrorMessage)
            }
          } else {
            onError(error)
          }
        },
      )
      .disposeBySelf()
  }

  fun login(userName: String, password: String) {
    loginUseCase(userName, password)
      .attachSilent()
      .doOnSubscribe { updateAuthorizationDialogState { it?.copy(processing = true) } }
      .doOnTerminate { updateAuthorizationDialogState { it?.copy(processing = false) } }
      .subscribeBy(
        onSuccess = {
          if (it.isAuthorized()) {
            onAuthorized(currentState().authorizationDialogState?.reason ?: AuthorizationReason.Default)
          } else {
            updateAuthorizationDialogState { state -> state?.copy(error = localizedString(R.string.status_unknown_err)) }
          }
        },
        onError = { error ->
          if (error is AuthorizationException) {
            updateAuthorizationDialogState { state ->
              state?.copy(error = error.localizedErrorMessage)
            }
          } else {
            onError(error)
          }
        }
      )
      .disposeBySelf()
  }

  fun closeAuthorizationDialog() {
    updateAuthorizationDialogState { null }
  }

  private fun isAuthorized(): Boolean {
    val (client) = guardLet(suplaClientProvider.provide()) { return false }
    return client.registered() && client.isSuperUserAuthorized()
  }
}

abstract class AuthorizationModelState : ViewState() {
  abstract val authorizationDialogState: AuthorizationDialogState?
}
