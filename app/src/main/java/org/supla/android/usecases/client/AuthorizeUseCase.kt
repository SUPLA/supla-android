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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.R
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConst
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizeUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider,
  private val suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  threadHandler: ThreadHandler
) : BaseCredentialsUseCase(threadHandler) {

  operator fun invoke(userName: String, password: String): Completable =
    Completable.fromRunnable {
      val (client) = guardLet(suplaClientProvider.provide()) {
        throw IllegalStateException("SuplaClient is null")
      }

      var authorized: Boolean? = null
      var error: Int? = null

      val listener: SuplaClientMessageHandler.OnSuplaClientMessageListener = getAuthorizeMessageListener(
        onAuthorized = { authorized = true },
        onError = { error = it }
      )

      suplaClientMessageHandlerWrapper.registerMessageListener(listener)
      client.superUserAuthorizationRequest(userName, password)

      try {
        waitForResponse(
          authorizedProvider = { authorized },
          errorProvider = { error }
        )
      } finally {
        suplaClientMessageHandlerWrapper.unregisterMessageListener(listener)
      }
    }

  private fun getAuthorizeMessageListener(onAuthorized: () -> Unit, onError: (Int) -> Unit) =
    object : SuplaClientMessageHandler.OnSuplaClientMessageListener {
      override fun onSuplaClientMessageReceived(msg: SuplaClientMsg?) {
        val (message) = guardLet(msg) { return }

        if (message.type == SuplaClientMsg.onSuperuserAuthorizationResult) {
          if (message.isSuccess) {
            onAuthorized()
          } else {
            onError(
              when (message.code) {
                SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED -> R.string.incorrect_email_or_password
                SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE -> R.string.status_temporarily_unavailable
                else -> R.string.status_unknown_err
              }
            )
          }

          // Unregister after first message, so the result doesn't get overwritten.
          suplaClientMessageHandlerWrapper.unregisterMessageListener(this)
        }
      }
    }
}
