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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val CA_FILE_NAME = "supla_org_cert.crt"

@Singleton
class EspCustomRootCaProvider @Inject constructor(
  @ApplicationContext private val context: Context
) {

  var customTrustManager: X509TrustManager? = null
  var sslContext: SSLContext? = null

  init {
    loadCertificateFromAssets()?.let { certificate ->
      val keyStoreType = KeyStore.getDefaultType()
      val keyStore = KeyStore.getInstance(keyStoreType).apply {
        load(null, null)
        setCertificateEntry("ca", certificate)
      }

      val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
      val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
        init(keyStore)
      }

      tmf.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()?.let { customTrustManager ->
        Timber.i("Supla Custom Root CA loaded successfully")
        this.customTrustManager = customTrustManager
        this.sslContext = SSLContext.getInstance("TLS").apply {
          init(null, arrayOf<TrustManager>(customTrustManager), null)
        }
      }
    }
  }

  private fun loadCertificateFromAssets(): X509Certificate? =
    try {
      val certificateFactory = CertificateFactory.getInstance("X.509")
      context.assets.open(CA_FILE_NAME).use { inputStream ->
        BufferedInputStream(inputStream).use { caInput ->
          certificateFactory.generateCertificate(caInput) as X509Certificate
        }
      }
    } catch (ex: Exception) {
      Timber.e(ex, "Could not generate certificate from assets")
      null
    }
}
