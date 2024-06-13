package org.supla.android.core

import android.content.Context
import org.supla.android.core.networking.suplaclient.SuplaClientApi

interface SuplaAppProvider {
  fun provide(): SuplaAppApi
}

interface SuplaAppApi {
  fun SuplaClientInitIfNeed(context: Context): SuplaClientApi?
  fun SuplaClientInitIfNeed(context: Context, oneTimePassword: String): SuplaClientApi?
  fun CancelAllRestApiClientTasks(mayInterruptIfRunning: Boolean)
  fun cleanupToken()
  fun getSuplaClient(): SuplaClientApi?
}
