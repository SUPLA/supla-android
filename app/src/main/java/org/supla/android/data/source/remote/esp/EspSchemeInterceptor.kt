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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EspSchemeInterceptor @Inject constructor(
  private var session: EspConfigurationSession
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response =
    if (session.useSecureLayer) {
      val request = chain.request()
      val newUrl = request.url.newBuilder().scheme("https").build()
      chain.proceed(request.newBuilder().url(newUrl).build())
    } else {
      chain.proceed(chain.request())
    }
}
