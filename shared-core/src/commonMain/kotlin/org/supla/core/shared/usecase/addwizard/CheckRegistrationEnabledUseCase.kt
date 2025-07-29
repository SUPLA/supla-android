@file:OptIn(ExperimentalTime::class)

package org.supla.core.shared.usecase.addwizard
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

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler
import org.supla.core.shared.networking.SuplaClientSharedProvider
import org.supla.core.shared.usecase.channel.isNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class CheckRegistrationEnabledUseCase(
  private val suplaClientMessageHandler: SuplaClientMessageHandler,
  private val suplaClientProvider: SuplaClientSharedProvider
) {

  suspend operator fun invoke(): Result {
    val semaphore = Semaphore(1, 1)
    val suplaMessageListener = MessageListener(semaphore)

    suplaClientMessageHandler.register(suplaMessageListener)
    suplaClientProvider.provide()?.getRegistrationEnabled()

    try {
      withTimeoutOrNull(10.seconds) {
        semaphore.acquire()
      }
    } finally {
      suplaClientMessageHandler.unregister(suplaMessageListener)
    }

    return suplaMessageListener.result ?: Result.TIMEOUT
  }

  private class MessageListener(private val semaphore: Semaphore) : SuplaClientMessageHandler.Listener {
    var result: Result? = null

    override fun onReceived(message: SuplaClientMessage) {
      (message as? SuplaClientMessage.RegistrationEnabled)?.let { registrationEnabled ->
        if (result.isNull()) {
          result =
            if (registrationEnabled.isDeviceRegistrationEnabled) {
              Result.ENABLED
            } else {
              Result.DISABLED
            }
        }
        semaphore.release()
      }
    }
  }

  enum class Result {
    ENABLED, DISABLED, TIMEOUT
  }
}
