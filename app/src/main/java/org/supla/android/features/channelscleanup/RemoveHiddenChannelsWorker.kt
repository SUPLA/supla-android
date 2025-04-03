package org.supla.android.features.channelscleanup
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
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase

@HiltWorker
class RemoveHiddenChannelsWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  private val removeHiddenChannelsUseCase: RemoveHiddenChannelsUseCase
) : CoroutineWorker(appContext, workerParameters) {

  override suspend fun doWork(): Result {
    return try {
      removeHiddenChannelsUseCase()
      Result.success()
    } catch (e: Throwable) {
      Trace.e(TAG, "Removing hidden channels broken!", e)
      Result.failure()
    }
  }

  companion object {
    val WORK_ID: String = RemoveHiddenChannelsWorker::class.java.simpleName
    val TAG: String = RemoveHiddenChannelsWorker::class.java.simpleName

    fun build(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<RemoveHiddenChannelsWorker>()
        .addTag(TAG)
        .build()
  }
}
