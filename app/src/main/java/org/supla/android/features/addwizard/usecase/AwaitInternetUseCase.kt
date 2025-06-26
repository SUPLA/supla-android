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

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 30.seconds

@Singleton
class AwaitInternetUseCase @Inject constructor(
  private val connectivityManager: ConnectivityManager
) {
  suspend operator fun invoke(): Result {
    val semaphore = Semaphore(1, 1)
    val request = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
      .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    val networkCallback = AwaitNetworkCallback(connectivityManager, semaphore)

    connectivityManager.requestNetwork(request, networkCallback)

    try {
      withTimeoutOrNull(TIMEOUT) {
        Trace.d(TAG, "Awaiting network availability")
        semaphore.acquire()
      }
    } finally {
      Trace.d(TAG, "Unregistering callback")
      connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    return networkCallback.result ?: Result.TIMEOUT
  }

  enum class Result {
    SUCCESS, TIMEOUT
  }
}

private class AwaitNetworkCallback(
  private val connectivityManager: ConnectivityManager,
  private val semaphore: Semaphore
) : ConnectivityManager.NetworkCallback() {
  var result: AwaitInternetUseCase.Result? = null

  override fun onAvailable(network: Network) {
    super.onAvailable(network)
    Trace.d(AwaitInternetUseCase::class.simpleName, "onAvailable($network)")

    connectivityManager.bindProcessToNetwork(network)
    result = AwaitInternetUseCase.Result.SUCCESS

    semaphore.release()
  }
}
