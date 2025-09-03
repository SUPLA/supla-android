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

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.extensions.allGranted
import org.supla.android.extensions.skipQuotation
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WiFiScanner @Inject constructor(
  @ApplicationContext context: Context,
  private val wifiManager: WifiManager
) {

  val cashedSsids: List<String>
    get() = ssids

  private val ssids = Collections.synchronizedList(mutableListOf<String>())
  private val semaphore = Semaphore(1, 1)
  private val mutex = Mutex()

  private var processingRequest = false

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      Trace.i(TAG, "Got intent: $intent")
      if (context?.allGranted(PERMISSIONS) == true) {
        handleResults()
      }
      if (processingRequest) {
        semaphore.release()
      }
    }
  }

  init {
    context.registerReceiver(broadcastReceiver, INTENT_FILTER)
  }

  suspend fun scan(): Result {
    mutex.withLock {
      Trace.i(TAG, "Scan started")
      processingRequest = true
      try {
        if (wifiManager.startScan()) {
          Trace.d(TAG, "Scan allowed")
          semaphore.acquire()
          Trace.d(TAG, "Scan finished")
          return Result.Success(ssids.toList())
        } else {
          Trace.d(TAG, "Scan not allowed")
          return Result.NotAllowed(ssids.toList())
        }
      } finally {
        processingRequest = false
      }
    }
  }

  private fun handleResults() {
    try {
      ssids.clear()
      ssids.addAll(
        wifiManager.scanResults.filter { it.frequency < 2500 }.mapNotNull { it.wifiSsidAsString }.distinct().filter { it.isNotEmpty() }
      )
    } catch (ex: SecurityException) {
      Trace.w(TAG, "Could not read WiFi scan results", ex)
    }
  }

  sealed interface Result {
    data class NotAllowed(val cashed: List<String>) : Result
    data class Success(val ssids: List<String>) : Result
  }

  companion object {
    private val PERMISSIONS = listOf(
      Manifest.permission.ACCESS_WIFI_STATE,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val INTENT_FILTER = IntentFilter().apply { addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) }
  }
}

@Suppress("DEPRECATION")
private val ScanResult.wifiSsidAsString: String?
  get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    wifiSsid?.toString()?.skipQuotation()
  } else {
    SSID?.skipQuotation()
  }
