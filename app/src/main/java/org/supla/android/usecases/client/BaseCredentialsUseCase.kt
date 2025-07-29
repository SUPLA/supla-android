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

import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.extensions.TAG
import org.supla.android.extensions.ifLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

private const val WAIT_TIME_MS: Long = 1000

open class BaseCredentialsUseCase(private val threadHandler: ThreadHandler) {
  protected fun waitForResponse(authorizedProvider: () -> Boolean?, errorMessageProvider: () -> LocalizedString?) {
    try {
      for (i in 0 until 10) {
        val authorized = authorizedProvider()
        val errorMessage = errorMessageProvider()

        ifLet(authorized) { _ ->
          return
        }
        ifLet(errorMessage) { (error) ->
          throw AuthorizationException.WithLocalizedString(error)
        }

        threadHandler.sleep(WAIT_TIME_MS)
      }
    } catch (exception: InterruptedException) {
      Trace.e(TAG, "Awaiting for response failed", exception)
      // Because of some reasons the process should stop so escape from the method
      return
    } catch (exception: AuthorizationException) {
      throw exception // just rethrow it
    } catch (exception: Exception) {
      Trace.e(TAG, "Awaiting for response failed", exception)
      throw AuthorizationException.WithResource(R.string.status_unknown_err)
    }

    throw AuthorizationException.WithResource(R.string.time_exceeded)
  }
}

sealed class AuthorizationException : Exception() {
  abstract val localizedErrorMessage: LocalizedString

  data class WithResource(@StringRes private val resourceId: Int) : AuthorizationException() {
    override val localizedErrorMessage: LocalizedString = localizedString(resourceId)
  }

  data class WithLocalizedString(override val localizedErrorMessage: LocalizedString) : AuthorizationException()
}
