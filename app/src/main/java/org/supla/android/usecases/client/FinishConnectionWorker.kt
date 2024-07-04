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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.BuildConfig
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.extensions.TAG
import org.supla.android.usecases.lock.GetLockScreenSettingUseCase

private const val BACKGROUND_ACTIVITY_TIME_DEBUG_S = 10
private const val BACKGROUND_ACTIVITY_TIME_S = 120

@HiltWorker
class FinishConnectionWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val disconnectUseCase: DisconnectUseCase,
  private val suplaClientStateHolder: SuplaClientStateHolder,
  private val getLockScreenSettingUseCase: GetLockScreenSettingUseCase,
) : Worker(appContext, workerParameters) {
  override fun doWork(): Result {
    Trace.d(TAG, "Starting supla client process terminator")
    timeout()

    if (isStopped) {
      Trace.d(TAG, "Supla client process terminator canceled")
    } else {
      disconnectUseCase.invokeSynchronous()
      Trace.d(TAG, "Supla client process terminated")

      if (getLockScreenSettingUseCase() == LockScreenScope.APPLICATION) {
        suplaClientStateHolder.handleEvent(SuplaClientEvent.Lock)
      } else {
        suplaClientStateHolder.handleEvent(SuplaClientEvent.Finish(SuplaClientState.Reason.AppInBackground))
      }
    }

    return Result.success()
  }

  override fun onStopped() {
    super.onStopped()
    Trace.d(TAG, "Supla client process terminator was active - stopping")
  }

  private fun timeout() {
    try {
      var iteration = 0
      val iterationsCount = getBackgroundActivityTime().times(4)
      while (iteration < iterationsCount && !isStopped) {
        Thread.sleep(250L)
        iteration++
      }
    } catch (exception: Exception) {
      // Ignore
    }
  }

  private fun getBackgroundActivityTime() =
    if (BuildConfig.DEBUG) {
      BACKGROUND_ACTIVITY_TIME_DEBUG_S
    } else {
      BACKGROUND_ACTIVITY_TIME_S
    }

  companion object {
    const val NAME = "FinishConnectionWorker"

    fun build(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<FinishConnectionWorker>().build()
  }
}
