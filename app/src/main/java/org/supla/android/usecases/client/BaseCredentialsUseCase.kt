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

import org.supla.android.R
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.extensions.ifLet

private const val WAIT_TIME_MS: Long = 1000

open class BaseCredentialsUseCase(private val threadHandler: ThreadHandler) {
  protected fun waitForResponse(authorizedProvider: () -> Boolean?, errorProvider: () -> Int?) {
    try {
      for (i in 0 until 10) {
        val authorized = authorizedProvider()
        val error = errorProvider()

        ifLet(authorized) { _ ->
          return
        }
        ifLet(error) { (error) ->
          throw AuthorizationException(error)
        }

        threadHandler.sleep(WAIT_TIME_MS)
      }
    } catch (exception: InterruptedException) {
      throw AuthorizationException(R.string.status_unknown_err)
    }

    throw AuthorizationException(R.string.time_exceeded)
  }
}

data class AuthorizationException(val messageId: Int?) : Exception()
