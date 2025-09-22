package org.supla.android.features.addwizard.usecase
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

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.extensions.TAG
import org.supla.android.extensions.isNull
import org.supla.android.features.addwizard.usecase.receiver.ConnectResult
import org.supla.android.features.addwizard.usecase.receiver.LegacyNetworkBroadcastReceiver
import org.supla.android.usecases.client.DisconnectUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 60.seconds
private const val INVALID_INT = -1

@Singleton
class ConnectToSsidUseCase @Inject constructor(
  private val disconnectUseCase: DisconnectUseCase,
  connectivityManager: ConnectivityManager,
  @ApplicationContext context: Context,
  wifiManager: WifiManager
) {

  private val mutex = Mutex()
  private val connectivityHandler = ConnectivityHandler(connectivityManager)
  private val legacyConnectivityHandler = LegacyConnectivityHandler(context, wifiManager)

  suspend fun connect(ssid: String): ConnectResult {
    mutex.withLock {
      disconnectUseCase.invokeSynchronous(SuplaClientState.Reason.AddWizardStarted)
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Trace.i(TAG, "Asking connectivity handler")
        connectivityHandler.connect(ssid)
      } else {
        Trace.i(TAG, "Asking legacy connectivity handler")
        try {
          legacyConnectivityHandler.connect(ssid)
        } catch (exception: SecurityException) {
          Trace.e(TAG, "Could not connect to ESP WiFi", exception)
          ConnectResult.FAILURE
        }
      }
    }
  }

  fun disconnect() {
    connectivityHandler.disconnect()
  }
}

private class ConnectivityHandler(
  private val connectivityManager: ConnectivityManager
) {
  private val semaphore = Semaphore(1, 1)
  private var result: ConnectResult? = null

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      super.onAvailable(network)

      if (result.isNull) {
        connectivityManager.bindProcessToNetwork(network)
        result = ConnectResult.SUCCESS
      }

      semaphore.release()
    }

    override fun onUnavailable() {
      super.onUnavailable()
      if (result.isNull) {
        result = ConnectResult.FAILURE
      }
      semaphore.release()
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  suspend fun connect(ssid: String): ConnectResult {
    result = null
    val specifier = WifiNetworkSpecifier.Builder().setSsid(ssid).build()
    val request = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
      .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .setNetworkSpecifier(specifier)
      .build()
    connectivityManager.requestNetwork(request, networkCallback)

    withTimeoutOrNull(TIMEOUT) {
      Trace.d(TAG, "Awaiting network availability")
      semaphore.acquire()
    }

    Trace.d(TAG, "Finishing with result $result")
    return result ?: ConnectResult.FAILURE
  }

  fun disconnect() {
    // Intentionally extracted, as after unregister the connection are not working
    connectivityManager.bindProcessToNetwork(null)
    connectivityManager.unregisterNetworkCallback(networkCallback)
  }
}

@Suppress("DEPRECATION")
private class LegacyConnectivityHandler(
  private val context: Context,
  private val wifiManager: WifiManager
) {
  @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
  suspend fun connect(ssid: String): ConnectResult {
    var networkId = INVALID_INT

    val wifiConfiguration = WifiConfiguration()
    wifiConfiguration.SSID = "\"$ssid\""
    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
    wifiConfiguration.priority = maxConfigurationPriority

    for (configuration in wifiManager.configuredNetworks) {
      if (configuration.SSID != null && (configuration.SSID == ssid || configuration.SSID == wifiConfiguration.SSID)) {
        networkId = configuration.networkId
        break
      }
    }

    if (networkId == INVALID_INT) {
      networkId = wifiManager.addNetwork(wifiConfiguration)
    }
    if (networkId == INVALID_INT) {
      throw IllegalStateException("Could not find network!")
    }

    val semaphore = Semaphore(1, 1)
    val changeReceiver = LegacyNetworkBroadcastReceiver(wifiManager, networkId, semaphore)

    wifiManager.disconnect()
    context.registerReceiver(changeReceiver, IntentFilter().apply { addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) })
    wifiManager.enableNetwork(networkId, true)
    wifiManager.reconnect()

    try {
      withTimeoutOrNull(TIMEOUT) {
        semaphore.acquire()
      }
    } finally {
      context.unregisterReceiver(changeReceiver)
    }

    return changeReceiver.result ?: ConnectResult.FAILURE
  }

  private val maxConfigurationPriority: Int
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    get() {
      val configurations = wifiManager.configuredNetworks
      return configurations.maxOfOrNull { it.priority } ?: 0
    }
}
