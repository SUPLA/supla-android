@file:Suppress("DEPRECATION")

package org.supla.android.features.addwizard.usecase.receiver
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.sync.Semaphore
import org.supla.android.extensions.isNull

class LegacyNetworkBroadcastReceiver(
  private val wifiManager: WifiManager,
  private val networkId: Int,
  private val semaphore: Semaphore
) : BroadcastReceiver() {

  var result: ConnectResult? = null

  override fun onReceive(context: Context?, intent: Intent?) {
    val info = intent?.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
    if (info?.isConnected != true) {
      return
    }

    val wifiInfo = wifiManager.connectionInfo
    if (wifiInfo.networkId != networkId) {
      return
    }

    if (result.isNull) {
      result = ConnectResult.SUCCESS
    }

    semaphore.release()
  }
}
