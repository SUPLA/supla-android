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

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.features.addwizard.usecase.receiver.ConnectResult
import org.supla.android.features.addwizard.usecase.receiver.LegacyNetworkBroadcastReceiver
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 30.seconds

@Singleton
class ReconnectToInternetUseCase @Inject constructor(
  @ApplicationContext private val context: Context,
  private val connectToSsidUseCase: ConnectToSsidUseCase,
  wifiManager: WifiManager
) {

  private val legacyEnableWifiUseCase = LegacyEnableWifiUseCase(context, wifiManager)

  suspend operator fun invoke(networkId: Int?): ConnectResult =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      connectToSsidUseCase.disconnect()
      ConnectResult.SUCCESS
    } else {
      networkId?.let {
        legacyEnableWifiUseCase.connect(it)
      } ?: ConnectResult.TIMEOUT
    }
}

@Suppress("DEPRECATION")
class LegacyEnableWifiUseCase(
  private val context: Context,
  private val wifiManager: WifiManager
) {
  suspend fun connect(networkId: Int): ConnectResult {
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

    Trace.d(TAG, "Finishing with result ${changeReceiver.result}")
    return changeReceiver.result ?: ConnectResult.TIMEOUT
  }
}
