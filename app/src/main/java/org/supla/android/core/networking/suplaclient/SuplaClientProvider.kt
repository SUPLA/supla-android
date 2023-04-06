package org.supla.android.core.networking.suplaclient

import org.supla.android.lib.SuplaClient

interface SuplaClientProvider {
  fun provide(): SuplaClient
}