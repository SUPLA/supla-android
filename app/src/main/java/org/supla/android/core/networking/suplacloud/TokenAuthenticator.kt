package org.supla.android.core.networking.suplacloud
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

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
  private val suplaCloudConfigHolder: SuplaCloudConfigHolder,
  private val suplaClientProvider: SuplaClientProvider
) : Authenticator {
  override fun authenticate(route: Route?, response: Response): Request? {
    return refreshedToken()?.let {
      return response.request.newBuilder()
        .header("Authorization", "Bearer $it")
        .build()
    }
  }

  private fun refreshedToken(): String? {
    return suplaCloudConfigHolder.token.let { token ->
      if (token != null && token.isAlive) {
        return@let token.token
      }

      suplaClientProvider.provide()?.oAuthTokenRequest()
      for (i in 0..50) {
        val tokenTry = suplaCloudConfigHolder.token
        if (tokenTry != null && tokenTry.isAlive) {
          return@let tokenTry.token
        }
        Thread.sleep(100)
      }

      return@let null
    }
  }
}
