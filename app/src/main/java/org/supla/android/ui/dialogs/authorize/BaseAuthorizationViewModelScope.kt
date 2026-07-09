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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.rx3.await
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ProfileRepository
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
import timber.log.Timber

interface BaseAuthorizationViewModelScope : AuthorizationDialogScope {
  val suplaClientProvider: SuplaClientProvider
  val profileRepository: ProfileRepository
  val loginUseCase: LoginUseCase
  val authorizeUseCase: AuthorizeUseCase
  val schedulers: SuplaSchedulers

  fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?)

  fun getAuthorizationDialogState(): AuthorizationDialogState?

  fun onAuthorized(reason: AuthorizationReason)

  fun launch(launcher: suspend CoroutineScope.() -> Unit)

  override fun onAuthorizationDismiss() {
    closeAuthorizationDialog()
  }

  override fun onAuthorizationCancel() {
    closeAuthorizationDialog()
  }

  override fun onAuthorize(userName: String, password: String) {
    authorize(userName, password)
  }

  fun onError(error: Throwable) {
    Timber.e(error, "Got error by authorization call!")
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

    launch {
      val profile = schedulers.io {
        runCatching { profileRepository.findActiveProfile().await() }.getOrNull()
      } ?: return@launch

      updateAuthorizationDialogState {
        it?.copy(
          userName = profile.email,
          isCloudAccount = profile.isCloudAccount,
          userNameEnabled = suplaClientProvider.provide()?.registered() == true,
          reason = reason,
          clarification = clarificationMessage
        ) ?: AuthorizationDialogState(
          userName = profile.email,
          isCloudAccount = profile.isCloudAccount,
          userNameEnabled = suplaClientProvider.provide()?.registered() == true,
          reason = reason,
          clarification = clarificationMessage
        )
      }
    }
  }

  fun authorize(userName: String, password: String) {
    launch {
      try {
        updateAuthorizationDialogState { it?.copy(processing = true) }
        val result = schedulers.io { authorizeUseCase(userName, password).await() }
        updateAuthorizationDialogState { it?.copy(processing = false) }

        // Success
        if (result.isAuthorized()) {
          onAuthorized(getAuthorizationDialogState()?.reason ?: AuthorizationReason.Default)
        } else {
          updateAuthorizationDialogState { state -> state?.copy(error = localizedString(R.string.status_unknown_err)) }
        }
      } catch (error: Exception) {
        // Failure
        if (error is AuthorizationException) {
          updateAuthorizationDialogState { state ->
            state?.copy(error = error.localizedErrorMessage, processing = false)
          }
        } else {
          updateAuthorizationDialogState { it?.copy(processing = false) }
          onError(error)
        }
      }
    }
  }

  fun login(userName: String, password: String) {
    launch {
      try {
        updateAuthorizationDialogState { it?.copy(processing = true) }
        val result = schedulers.io { loginUseCase(userName, password).await() }
        updateAuthorizationDialogState { it?.copy(processing = false) }

        // Success
        if (result.isAuthorized()) {
          onAuthorized(getAuthorizationDialogState()?.reason ?: AuthorizationReason.Default)
        } else {
          updateAuthorizationDialogState { state -> state?.copy(error = localizedString(R.string.status_unknown_err)) }
        }
      } catch (error: Exception) {
        // Failure
        if (error is AuthorizationException) {
          updateAuthorizationDialogState { state ->
            state?.copy(error = error.localizedErrorMessage, processing = false)
          }
        } else {
          updateAuthorizationDialogState { it?.copy(processing = false) }
          onError(error)
        }
      }
    }
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
