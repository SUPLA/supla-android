package org.supla.android.features.addwizard.usecase
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
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.SuplaResultCode
import org.supla.android.extensions.isNull
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.core.shared.extensions.ifTrue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private const val DEVICE_REGISTRATION_TIME = 3600

@Singleton
class EnableRegistrationUseCase @Inject constructor(
  private val suplaClientMessageHandler: SuplaClientMessageHandler,
  private val suplaClientProvider: SuplaClientProvider
) {
  suspend operator fun invoke(): Result {
    val semaphore = Semaphore(1, 1)
    val suplaMessageListener = MessageListener(semaphore)

    suplaClientMessageHandler.registerMessageListener(suplaMessageListener)
    suplaClientProvider.provide()?.setRegistrationEnabled(DEVICE_REGISTRATION_TIME, -1)

    try {
      withTimeoutOrNull(10.seconds) {
        semaphore.acquire()
      }
    } finally {
      suplaClientMessageHandler.unregisterMessageListener(suplaMessageListener)
    }

    return suplaMessageListener.result ?: Result.TIMEOUT
  }

  private class MessageListener(private val semaphore: Semaphore) : OnSuplaClientMessageListener {
    var result: Result? = null

    override fun onSuplaClientMessageReceived(message: SuplaClientMsg?) {
      if (message?.type == SuplaClientMsg.onSetRegistrationEnabledResult) {
        if (result.isNull) {
          result = (message.code == SuplaResultCode.TRUE.value).ifTrue { Result.SUCCESS } ?: Result.FAILURE
        }
        semaphore.release()
      }
    }
  }

  enum class Result {
    SUCCESS, FAILURE, TIMEOUT
  }
}
