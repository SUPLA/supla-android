package org.supla.android.usecases.client
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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
  @ApplicationContext private val applicationContext: Context,
  private val suplaAppProvider: SuplaAppProvider,
  private val suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  threadHandler: ThreadHandler
) : BaseCredentialsUseCase(threadHandler) {

  operator fun invoke(userName: String, password: String): Single<Result> =
    Single.fromCallable {
      val (client) = guardLet(suplaAppProvider.provide().SuplaClientInitIfNeed(applicationContext, password)) {
        throw IllegalStateException("SuplaClient is null")
      }

      var authorized: Boolean? = null
      var error: Int? = null

      val listener: SuplaClientMessageHandler.OnSuplaClientMessageListener = getLoginMessageListener(
        onAuthorized = { authorized = true },
        onError = { error = it }
      )

      suplaClientMessageHandlerWrapper.registerMessageListener(listener)
      client.superUserAuthorizationRequest(userName, password)

      try {
        waitForResponse(
          authorizedProvider = { authorized },
          errorProvider = { error },
          isLogin = true
        )
      } finally {
        suplaClientMessageHandlerWrapper.unregisterMessageListener(listener)
      }

      return@fromCallable if (authorized == true) {
        Result.Authorized
      } else {
        Result.Unauthorized
      }
    }

  private fun getLoginMessageListener(onAuthorized: () -> Unit, onError: (Int) -> Unit) =
    object : SuplaClientMessageHandler.OnSuplaClientMessageListener {
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

  sealed interface Result {
    fun isAuthorized(): Boolean

    data object Authorized : Result {
      override fun isAuthorized() = true
    }
    data object Unauthorized : Result {
      override fun isAuthorized() = false
    }
  }
}
