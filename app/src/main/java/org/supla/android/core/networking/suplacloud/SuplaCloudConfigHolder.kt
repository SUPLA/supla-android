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

import android.annotation.SuppressLint
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaOAuthToken
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class SuplaCloudConfigHolder @Inject constructor() {

  var token: SuplaOAuthToken? = null

  var url: String? = null

  val socketFactory: SSLSocketFactory?
    get() {
      return try {
        SSLContext.getInstance("TLS")
          .also { it.init(null, trustManagers, SecureRandom()) }
          .socketFactory
      } catch (ex: NoSuchAlgorithmException) {
        Trace.e(TAG, ex.message, ex)
        null
      }
    }

  val trustManagers: Array<TrustManager>?
    get() {
      val (url) = guardLet(token?.url) { return null }

      if (url.authority.contains(".supla.org")) {
        return null
      }

      return arrayOf(

        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {

          @SuppressLint("TrustAllX509TrustManager")
          override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
          }

          override fun checkServerTrusted(certs: Array<out X509Certificate>?, authType: String?) {
            if (certs == null) {
              throw IllegalArgumentException("No certificates!")
            }
            if (certs.size > 1) {
              val factory: TrustManagerFactory = try {
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).also {
                  it.init(null as KeyStore?)
                }
              } catch (ex: NoSuchAlgorithmException) {
                throw CertificateException("Can't verify certificate", ex)
              }

              for (manager in factory.trustManagers) {
                if (manager is X509TrustManager) {
                  manager.checkServerTrusted(certs, authType)
                }
              }
            }
          }

          override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
          }
        }
      )
    }

  fun clean() {
    token = null
    url = null
  }
}
