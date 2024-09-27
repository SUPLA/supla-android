package org.supla.android.features.icons
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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.usecases.icon.LoadUserIconsIntoCacheUseCase
import org.supla.android.widget.WidgetManager

@HiltWorker
class LoadUserIconsIntoCacheWorker @AssistedInject constructor(
  private val loadUserIconsIntoCacheUseCase: LoadUserIconsIntoCacheUseCase,
  private val widgetManager: WidgetManager,
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {
  override fun doWork(): Result {
    return try {
      val statistics = loadUserIconsIntoCacheUseCase().blockingGet()
      if (statistics.iconsCount > 0 && statistics.changed) {
        widgetManager.updateAllWidgets()
      }
      Result.success()
    } catch (ex: Exception) {
      Trace.e(TAG, "Load user icons into cache worker failed!")
      Result.failure()
    }
  }

  companion object {
    private val WORK_ID: String = LoadUserIconsIntoCacheWorker::class.java.simpleName

    fun start(context: Context) {
      WorkManager.getInstance(context).enqueueUniqueWork(WORK_ID, ExistingWorkPolicy.KEEP, build())
    }

    private fun build(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<LoadUserIconsIntoCacheWorker>()
        .build()
  }
}
