package org.supla.android.data.source.remote.esp
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

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val COOKIE_SESSION_NAME = "session"

@Singleton
class EspCookiesInterceptor @Inject constructor(
  private val session: EspConfigurationSession
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val sessionCookie = session.sessionCookie
    val request =
      if (sessionCookie == null) {
        chain.request()
      } else {
        Timber.i("Setting session cookie `$sessionCookie`")
        chain.request().newBuilder()
          .addHeader("Cookie", sessionCookie)
          .build()
      }

    val response = chain.proceed(request)

    for (cookie in response.headers("Set-Cookie")) {
      Timber.i("Got new cookie: $cookie")

      val cookieParts = cookie.split(";")
      if (cookieParts.isEmpty()) {
        continue
      }
      val cookieValue = cookieParts[0]
      if (cookieValue.startsWith("$COOKIE_SESSION_NAME=")) {
        Timber.i("Overtaking session cookie `$cookieValue`")
        session.sessionCookie = cookieValue
      }
    }

    session.lastAuthStatus = response.header("Auth-Status")?.let { EspConfigurationSession.AuthStatus.from(it) }

    return response
  }
}
