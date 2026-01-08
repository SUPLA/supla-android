package org.supla.android.widget.onoff
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.notifications.ON_OFF_WIDGET_NOTIFICATION_ID
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.VibrationHelper
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.WidgetAction
import org.supla.android.widget.shared.WidgetCommandWorkerBase
import org.supla.android.widget.shared.WidgetConfigurationUpdater
import org.supla.android.widget.shared.getWorkId
import timber.log.Timber

private const val WORK_ID_PREFIX = "ON_OFF_WIDGET_"

@HiltWorker
class OnOffWidgetCommandWorker @AssistedInject constructor(
  widgetConfigurationUpdater: WidgetConfigurationUpdater,
  notificationsHelper: NotificationsHelper,
  singleCallProvider: SingleCall.Provider,
  widgetPreferences: WidgetPreferences,
  vibrationHelper: VibrationHelper,
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters
) : WidgetCommandWorkerBase(
  widgetConfigurationUpdater,
  notificationsHelper,
  singleCallProvider,
  widgetPreferences,
  vibrationHelper,
  appContext,
  workerParams
) {

  override val notificationId = ON_OFF_WIDGET_NOTIFICATION_ID

  override fun sendWidgetRedrawAction(widgetId: Int) =
    applicationContext.sendBroadcast(intent(applicationContext, WidgetAction.REDRAW.string, widgetId))

  override fun valueWithUnit(): Boolean = true

  companion object {
    fun enqueue(widgetIds: IntArray, widgetAction: WidgetAction, workManagerProxy: WorkManagerProxy) {
      val workName = getWorkId(WORK_ID_PREFIX, widgetIds)
      Timber.d("Enqueueing single widget command worker $workName")

      val inputData = buildInputData(widgetIds, widgetAction)

      val removeWidgetsWork = OneTimeWorkRequestBuilder<OnOffWidgetCommandWorker>()
        .setInputData(inputData)
        .build()

      // Work for widget ID is unique, so no other worker for the same ID will be started
      workManagerProxy.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, removeWidgetsWork)
    }
  }
}
