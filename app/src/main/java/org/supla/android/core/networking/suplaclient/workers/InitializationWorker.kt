package org.supla.android.core.networking.suplaclient.workers
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
import org.supla.android.usecases.app.InitializationUseCase

@HiltWorker
class InitializationWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  private val initializationUseCase: InitializationUseCase
) : Worker(appContext, workerParameters) {

  override fun doWork(): Result {
    initializationUseCase(appContext)
    return Result.success()
  }

  companion object {
    const val NAME = "InitializationWorker"

    fun build(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<InitializationWorker>().build()
  }
}
