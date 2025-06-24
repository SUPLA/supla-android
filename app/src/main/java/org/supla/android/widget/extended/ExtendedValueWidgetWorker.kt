package org.supla.android.widget.extended
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
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.supla.android.Trace
import org.supla.android.data.source.local.dao.WidgetConfigurationDao
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.widget.extended.ExtendedValueWidget.Companion.TAG
import org.supla.android.widget.extended.value.ExtendedValueWidgetProvider
import java.util.Date
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@HiltWorker
class ExtendedValueWidgetWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted workerParameters: WorkerParameters,
  private val widgetConfigurationDao: WidgetConfigurationDao,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val extendedValueWidgetProvider: ExtendedValueWidgetProvider
) : CoroutineWorker(context, workerParameters) {

  private val glanceId: String?
    get() = inputData.getString(GLANCE_ID)

  override suspend fun doWork(): Result {
    Trace.d(TAG, "Extended value widget worker started (glance id: `$glanceId`)")

    val appWidgetManager = GlanceAppWidgetManager(context = context)
    appWidgetManager.getGlanceIds(ExtendedValueWidget::class.java)
      .filter { glanceId == null || it.toString() == glanceId }
      .forEach { glanceId ->

        Trace.d(TAG, "Extended value widget worker updating widget with id `$glanceId`")
        val configuration = withContext(Dispatchers.IO) { widgetConfigurationDao.findBy(glanceId.toString()) }

        configuration?.let {
          val value = withContext(Dispatchers.IO) { extendedValueWidgetProvider.provide(configuration) }

          Trace.d(TAG, "Extended value widget worker setting configuration `$configuration`")
          val state = ExtendedValueWidgetState(
            icon = configuration.icon(getChannelIconUseCase, getSceneIconUseCase),
            caption = configuration.widgetConfiguration.caption,
            function = configuration.function,
            value = value,
            updateTime = Date().time
          )

          updateAppWidgetState(
            context = context,
            definition = ExtendedValueWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { state }
          )

          ExtendedValueWidget().update(context, glanceId)
        }
      }

    return Result.success()
  }

  companion object {
    private val PERIODIC_WORK_ID = "PERIODIC_${ExtendedValueWidgetWorker::class.java.simpleName}"
    private val UNIQUE_WORK_ID = "UNIQUE_${ExtendedValueWidgetWorker::class.java.simpleName}"

    private val GLANCE_ID = "GLANCE_ID"

    fun enqueuePeriodic(workManager: WorkManager) {
      workManager.enqueueUniquePeriodicWork(
        PERIODIC_WORK_ID,
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<ExtendedValueWidgetWorker>(15.minutes.toJavaDuration())
          .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
          .build()
      )
    }

    fun singleRun(workManager: WorkManager, glanceId: GlanceId? = null) {
      workManager.enqueueUniqueWork(
        UNIQUE_WORK_ID,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<ExtendedValueWidgetWorker>()
          .setInputData(
            Data.Builder()
              .putString(GLANCE_ID, glanceId.toString())
              .build()
          )
          .build()
      )
    }
  }
}
