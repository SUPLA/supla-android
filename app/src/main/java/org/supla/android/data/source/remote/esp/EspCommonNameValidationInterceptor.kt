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
import java.io.IOException
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EspCommonNameValidationInterceptor @Inject constructor(
  private val session: EspConfigurationSession
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response =
    if (session.useSecureLayer) {
      val response = chain.proceed(chain.request())
      val commonNameHeader = response.headers["CN"]
      val commonNameCertificate = findCommonNameInCertificate(response)

      if (commonNameHeader != null && commonNameCertificate != null && commonNameHeader == commonNameCertificate) {
        response
      } else {
        throw IOException("Different common names (certificate `$commonNameCertificate`, response `$commonNameHeader`)")
      }
    } else {
      chain.proceed(chain.request())
    }

  private fun findCommonNameInCertificate(response: Response): String? {
    response.handshake?.let { handshake ->
      for (certificate in handshake.peerCertificates) {
        (certificate as? X509Certificate)?.let {
          return extractCnFromCertificate(it)
        }
      }
    }

    return null
  }

  fun extractCnFromCertificate(certificate: X509Certificate): String? {
    val subjectDn = certificate.subjectX500Principal.name
    val cnPattern = "CN=([^,]+)".toRegex()
    val matchResult = cnPattern.find(subjectDn)
    return matchResult?.groups?.get(1)?.value
  }
}
