package org.supla.android.core.observers
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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.BuildConfig
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.extensions.TAG
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

private const val BACKGROUND_ACTIVITY_TIME_DEBUG_S = 10
private const val BACKGROUND_ACTIVITY_TIME_S = 120

@Singleton
class AppLifecycleObserver @Inject constructor(
  private val getLockScreenSettingUseCase: GetLockScreenSettingUseCase,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val disconnectUseCase: DisconnectUseCase,
) : DefaultLifecycleObserver {

  private var clientTerminator: Job? = null

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
    if (clientTerminator?.isActive == true) {
      Trace.d(TAG, "Supla client process terminator was active - stopping")
      clientTerminator?.cancel()
    }

    suplaClientStateHolder.handleEvent(SuplaClientEvent.OnStart)
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    clientTerminator = GlobalScope.launch {
      Trace.d(TAG, "Starting supla client process terminator")
      delay(getBackgroundActivityTime().seconds)
      disconnectUseCase.invokeSynchronous()
      Trace.d(TAG, "Supla client process terminated")

      if (getLockScreenSettingUseCase() == LockScreenScope.APPLICATION) {
        suplaClientStateHolder.handleEvent(SuplaClientEvent.Lock)
      } else {
        suplaClientStateHolder.handleEvent(SuplaClientEvent.Finish(SuplaClientState.Reason.AppInBackground))
      }
    }
  }

  private fun getBackgroundActivityTime() =
    if (BuildConfig.DEBUG) {
      BACKGROUND_ACTIVITY_TIME_DEBUG_S
    } else {
      BACKGROUND_ACTIVITY_TIME_S
    }
}
