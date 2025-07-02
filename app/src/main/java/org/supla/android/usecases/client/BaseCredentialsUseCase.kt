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
import org.supla.android.data.source.remote.SuplaResultCode
import org.supla.android.extensions.TAG
import org.supla.android.extensions.ifLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

private const val WAIT_TIME_MS: Long = 1000

open class BaseCredentialsUseCase(private val threadHandler: ThreadHandler) {
  protected fun waitForResponse(authorizedProvider: () -> Boolean?, errorProvider: () -> Int?, isLogin: Boolean) {
    try {
      for (i in 0 until 10) {
        val authorized = authorizedProvider()
        val error = errorProvider()

        ifLet(authorized) { _ ->
          return
        }
        ifLet(error) { (error) ->
          throw AuthorizationException.WithErrorCode(error, isLogin)
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
  abstract val localizedErrorMessange: LocalizedString

  data class WithResource(@StringRes private val resourceId: Int) : AuthorizationException() {
    override val localizedErrorMessange: LocalizedString = localizedString(resourceId)
  }

  data class WithErrorCode(private val errorCode: Int, private val isLogin: Boolean) : AuthorizationException() {
    override val localizedErrorMessange: LocalizedString
      get() = when (errorCode) {
        SuplaResultCode.TEMPORARILY_UNAVAILABLE.value -> localizedString(R.string.status_temporarily_unavailable)
        SuplaResultCode.CLIENT_LIMIT_EXCEEDED.value -> localizedString(R.string.status_climit_exceded)
        SuplaResultCode.CLIENT_DISABLED.value -> localizedString(R.string.status_device_disabled)
        SuplaResultCode.ACCESS_ID_DISABLED.value -> localizedString(R.string.status_accessid_disabled)
        SuplaResultCode.REGISTRATION_DISABLED.value -> localizedString(R.string.status_reg_disabled)
        SuplaResultCode.ACCESS_ID_NOT_ASSIGNED.value -> localizedString(R.string.status_access_id_not_assigned)
        SuplaResultCode.INACTIVE.value -> localizedString(R.string.status_accessid_inactive)
        SuplaResultCode.BAD_CREDENTIALS.value ->
          localizedString(if (isLogin) R.string.incorrect_email_or_password else R.string.status_bad_credentials)
        else -> LocalizedString.WithResourceAndString(R.string.status_unknown_err, " ($errorCode)")
      }
  }
}
