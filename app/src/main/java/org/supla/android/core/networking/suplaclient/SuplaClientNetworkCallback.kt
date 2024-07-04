package org.supla.android.core.networking.suplaclient

import android.net.ConnectivityManager
import android.net.Network
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuplaClientNetworkCallback @Inject constructor(
  private val suplaClientStateHolder: SuplaClientStateHolder
) : ConnectivityManager.NetworkCallback() {
  override fun onAvailable(network: Network) {
    suplaClientStateHolder.handleEvent(SuplaClientEvent.NetworkConnected)
  }
}
