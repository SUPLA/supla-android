package org.supla.android.core.networking.suplaclient

interface SuplaClientProvider {
  fun provide(): SuplaClientApi?
}

interface SuplaClientApi {
  fun cancel()

  @Throws(InterruptedException::class)
  fun join()

  fun reconnect()

  fun open(ID: Int, Group: Boolean, Open: Int): Boolean
}
