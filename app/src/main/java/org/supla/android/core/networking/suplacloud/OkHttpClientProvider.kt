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

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.X509TrustManager

@Singleton
class OkHttpClientProvider @Inject constructor(
  private val oauthInterceptor: AuthInterceptor,
  private val suplaCloudConfigHolder: SuplaCloudConfigHolder,
  private val tokenAuthenticator: TokenAuthenticator
) {

  fun provide() =
    OkHttpClient.Builder()
      .addInterceptor(oauthInterceptor)
      .addInterceptor(
        HttpLoggingInterceptor().also {
          it.level = HttpLoggingInterceptor.Level.BODY
        }
      )
      .authenticator(tokenAuthenticator)
      .also {
        val socketFactory = suplaCloudConfigHolder.socketFactory
        val trustManagers = suplaCloudConfigHolder.trustManagers

        // Used only by private supla cloud instances
        if (socketFactory != null && trustManagers != null) {
          it.sslSocketFactory(socketFactory, trustManagers[0] as X509TrustManager)
          it.hostnameVerifier { _, _ -> true }
        }
      }
      .build()
}
