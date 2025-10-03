package org.supla.android.usecases.client
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.events.UpdateEventsManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisconnectUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider,
  private val suplaAppProvider: SuplaAppProvider,
  private val updateEventsManager: UpdateEventsManager,
) {

  operator fun invoke(reason: SuplaClientState.Reason? = null): Completable = Completable.fromRunnable {
    invokeSynchronous(reason)
  }

  fun invokeSynchronous(reason: SuplaClientState.Reason? = null) {
    val suplaClient = suplaClientProvider.provide()
    if (suplaClient != null && suplaClient.canceled().not()) {
      suplaClient.cancel(reason)
      try {
        suplaClient.join()
      } catch (ex: InterruptedException) {
        Timber.w(ex, "Supla client thread joining broken by InterruptedException")
      }
    } else {
      // It may happen, that supla client is not running but created
      // If it will be marked as canceled, supla app will initialize it again.
      suplaClient?.cancel()
    }

    val suplaApp = suplaAppProvider.provide()
    suplaApp.CancelAllRestApiClientTasks(true)
    suplaApp.cleanupToken()

    updateEventsManager.cleanup()
    updateEventsManager.emitChannelsUpdate()
    updateEventsManager.emitGroupsUpdate()
    updateEventsManager.emitScenesUpdate()
  }
}
