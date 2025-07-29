package org.supla.android.core.infrastructure
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
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.Trace
import org.supla.android.extensions.skipQuotation
import org.supla.core.shared.extensions.ifTrue
import javax.inject.Inject
import javax.inject.Singleton

private const val UNKNOWN_SSID = "<unknown ssid>"

@Singleton
class CurrentWifiNetworkInfoProvider @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private var networkInfo: NetworkInfo? = null
  private var registered: Boolean = false

  private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
  private val wifiManager = context.getSystemService(WifiManager::class.java)

  fun register() {
    if (!registered && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val request =
        NetworkRequest.Builder()
          .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
          .build()

      val networkCallback = object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
          val wifiInfo = networkCapabilities.transportInfo as? WifiInfo
          Trace.i(CurrentWifiNetworkInfoProvider::class.simpleName, "onCapabilitiesChanged ${wifiInfo?.ssid}")
          wifiInfo?.let {
            networkInfo = NetworkInfo(
              it.ssid?.skipQuotation()?.skipUnknownSsid(),
              it.networkId
            )
          }
        }
      }
      connectivityManager.registerNetworkCallback(request, networkCallback)

      registered = true
    }
  }

  fun provide(): NetworkInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      networkInfo
    } else {
      legacyNetworkInfo()
    }

  @Suppress("DEPRECATION")
  private fun legacyNetworkInfo(): NetworkInfo? =
    wifiManager.isWifiEnabled.ifTrue {
      wifiManager.connectionInfo?.let {
        NetworkInfo(
          ssid = it.ssid?.skipQuotation()?.skipUnknownSsid(),
          networkId = it.networkId
        )
      }
    }

  data class NetworkInfo(
    val ssid: String?,
    val networkId: Int
  )
}

private fun String.skipUnknownSsid(): String? =
  if (this == UNKNOWN_SSID) {
    null
  } else {
    this
  }
