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

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED
import org.supla.android.lib.SuplaRegisterError
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState

abstract class BaseAuthorizationViewModel<S : AuthorizationModelState, E : ViewEvent>(
  private val suplaClientProvider: SuplaClientProvider,
  private val profileRepository: RoomProfileRepository,
  private val suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  defaultState: S,
  schedulers: SuplaSchedulers,
  private val sleepTime: Long = 1000
) : BaseViewModel<S, E>(defaultState, schedulers) {

  protected abstract fun updateDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?)

  abstract fun onAuthorized()

  open fun onError(error: Throwable) {
    defaultErrorHandler("authorize")(error)
  }

  fun updateAuthorizationState(state: AuthorizationDialogState) {
    updateDialogState { state }
  }

  fun showAuthorizationDialog() {
    if (isAuthorized()) {
      onAuthorized()
      return
    }

    profileRepository.findActiveProfile()
      .attach()
      .subscribeBy(
        onSuccess = { profile ->
          updateDialogState {
            it?.copy(
              userName = profile.email ?: "",
              isCloudAccount = profile.serverForEmail?.contains(".supla.org") == true,
              userNameEnabled = suplaClientProvider.provide()?.registered() == true
            ) ?: AuthorizationDialogState(
              userName = profile.email ?: "",
              isCloudAccount = profile.serverForEmail?.contains(".supla.org") == true,
              userNameEnabled = suplaClientProvider.provide()?.registered() == true
            )
          }
        }
      )
      .disposeBySelf()
  }

  fun authorize(userName: String, password: String) {
    val (client) = guardLet(suplaClientProvider.provide()) {
      onError(IllegalStateException("SuplaClient is null"))
      return
    }

    Observable.fromCallable {
      var authorized: Boolean? = null
      var error: Int? = null

      val listener: OnSuplaClientMessageListener = getAuthorizeMessageListener(
        onAuthorized = { authorized = true },
        onError = { error = it }
      )

      suplaClientMessageHandlerWrapper.registerMessageListener(listener)
      client.superUserAuthorizationRequest(userName, password)

      val response = waitForResponse(
        authorizedProvider = { authorized },
        errorProvider = { error }
      )
      suplaClientMessageHandlerWrapper.unregisterMessageListener(listener)
      return@fromCallable response
    }
      .attachSilent()
      .doOnSubscribe { updateDialogState { it?.copy(processing = true) } }
      .doOnTerminate { updateDialogState { it?.copy(processing = false) } }
      .subscribeBy(
        onNext = { result ->
          if (result.success) {
            onAuthorized()
          } else {
            updateDialogState { state ->
              state?.copy(error = { it.getString(result.error ?: R.string.status_unknown_err) })
            }
          }
        },
        onError = this::onError
      )
      .disposeBySelf()
  }

  /**
   * Prepared but not used and not tested!
   */
  fun login(userName: String, password: String) {
    val (client) = guardLet(suplaClientProvider.provide()) { return }

    Observable.fromCallable {
      var authorized: Boolean? = null
      var error: Int? = null

      val listener: OnSuplaClientMessageListener = getLoginMessageListener(
        onAuthorized = { authorized = true },
        onError = { error = it }
      )

      suplaClientMessageHandlerWrapper.registerMessageListener(listener)
      client.superUserAuthorizationRequest(userName, password)

      val response = waitForResponse(
        authorizedProvider = { authorized },
        errorProvider = { error }
      )

      suplaClientMessageHandlerWrapper.unregisterMessageListener(listener)
      return@fromCallable response
    }
      .attachSilent()
      .doOnSubscribe { updateDialogState { it?.copy(processing = true) } }
      .doOnTerminate { updateDialogState { it?.copy(processing = false) } }
      .subscribeBy(
        onNext = { result ->
          if (result.success) {
            onAuthorized()
          } else {
            updateDialogState { state ->
              val registerError = SuplaRegisterError().also { it.ResultCode = result.error!! }
              state?.copy(error = { registerError.codeToString(it, true) })
            }
          }
        },
        onError = this::onError
      )
      .disposeBySelf()
  }

  fun hideAuthorizationDialog() {
    updateDialogState { null }
  }

  private fun isAuthorized(): Boolean {
    val (client) = guardLet(suplaClientProvider.provide()) { return false }
    return client.registered() && client.isSuperUserAuthorized()
  }

  private fun waitForResponse(authorizedProvider: () -> Boolean?, errorProvider: () -> Int?): AuthorizationResult {
    try {
      for (i in 0 until 10) {
        val authorized = authorizedProvider()
        val error = errorProvider()

        if (authorized != null || error != null) {
          return AuthorizationResult(authorized ?: false, error)
        }

        Thread.sleep(sleepTime)
      }
    } catch (exception: InterruptedException) {
      return AuthorizationResult(false, R.string.status_unknown_err)
    }

    return AuthorizationResult(false, R.string.time_exceeded)
  }

  private fun getAuthorizeMessageListener(onAuthorized: () -> Unit, onError: (Int) -> Unit) =
    object : OnSuplaClientMessageListener {
      override fun onSuplaClientMessageReceived(msg: SuplaClientMsg?) {
        val (message) = guardLet(msg) { return }

        if (message.type == SuplaClientMsg.onSuperuserAuthorizationResult) {
          if (message.isSuccess) {
            onAuthorized()
          } else {
            onError(
              when (message.code) {
                SUPLA_RESULTCODE_UNAUTHORIZED -> R.string.incorrect_email_or_password
                SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE -> R.string.status_temporarily_unavailable
                else -> R.string.status_unknown_err
              }
            )
          }

          // Unregister after first message, so the result doesn't get overwritten.
          suplaClientMessageHandlerWrapper.unregisterMessageListener(this)
        }
      }
    }

  private fun getLoginMessageListener(onAuthorized: () -> Unit, onError: (Int) -> Unit) =
    object : OnSuplaClientMessageListener {
      override fun onSuplaClientMessageReceived(msg: SuplaClientMsg?) {
        val (message) = guardLet(msg) { return }

        if (message.type == SuplaClientMsg.onRegisterError) {
          onError(message.registerError.ResultCode)
          // Unregister after first message, so the result doesn't get overwritten.
          suplaClientMessageHandlerWrapper.unregisterMessageListener(this)
        } else if (message.type == SuplaClientMsg.onRegistered) {
          onAuthorized()
          // Unregister after first message, so the result doesn't get overwritten.
          suplaClientMessageHandlerWrapper.unregisterMessageListener(this)
        }
      }
    }

  private data class AuthorizationResult(
    val success: Boolean,
    val error: Int?
  )
}

abstract class AuthorizationModelState : ViewState() {
  abstract val authorizationDialogState: AuthorizationDialogState?
}
