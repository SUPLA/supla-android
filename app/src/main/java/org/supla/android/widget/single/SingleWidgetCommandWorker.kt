package org.supla.android.widget.single
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

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.notifications.SINGLE_WIDGET_NOTIFICATION_ID
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetCommandWorkerBase
import org.supla.android.widget.shared.getWorkId
import org.supla.core.shared.data.model.general.SuplaFunction

/**
 * Worker which is implemented for turning on/off switches. It supports following channel functions:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 * power switch [SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH]
 * It supports also opening and closing of roller shutters
 */
@HiltWorker
class SingleWidgetCommandWorker @AssistedInject constructor(
  loadChannelConfigUseCase: LoadChannelConfigUseCase,
  notificationsHelper: NotificationsHelper,
  vibrationHelper: VibrationHelper,
  appPreferences: Preferences,
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters
) : WidgetCommandWorkerBase(loadChannelConfigUseCase, notificationsHelper, vibrationHelper, appPreferences, appContext, workerParams) {

  override val notificationId = SINGLE_WIDGET_NOTIFICATION_ID

  override fun updateWidget(widgetId: Int) = updateSingleWidget(applicationContext, widgetId)
  override fun valueWithUnit(): Boolean = false

  override fun perform(widgetId: Int, configuration: WidgetConfiguration): Result {
    if (configuration.subjectType == SubjectType.SCENE) {
      if (configuration.actionId != null) {
        callAction(configuration, configuration.actionId)
      }
    } else {
      when (configuration.subjectFunction) {
        SuplaFunction.CONTROLLING_THE_GATE,
        SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> if (configuration.actionId != null) {
          callAction(configuration, configuration.actionId)
        }

        SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
        SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> callAction(configuration, ActionId.OPEN)

        SuplaFunction.LIGHTSWITCH,
        SuplaFunction.POWER_SWITCH,
        SuplaFunction.STAIRCASE_TIMER,
        SuplaFunction.DIMMER,
        SuplaFunction.DIMMER_AND_RGB_LIGHTING,
        SuplaFunction.RGB_LIGHTING -> {
          if (configuration.actionId == null) {
            return callCommon(widgetId, configuration)
          }
          callAction(configuration, configuration.actionId)
        }

        else -> return callCommon(widgetId, configuration)
      }
    }
    return Result.success()
  }

  private fun callCommon(widgetId: Int, configuration: WidgetConfiguration): Result {
    return performCommon(widgetId, configuration, configuration.actionId)
  }

  companion object {
    fun enqueue(widgetIds: IntArray, workManagerProxy: WorkManagerProxy) {
      val inputData = Data.Builder()
        .putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        .build()

      val removeWidgetsWork = OneTimeWorkRequestBuilder<SingleWidgetCommandWorker>()
        .setInputData(inputData)
        .build()

      // Work for widget ID is unique, so no other worker for the same ID will be started
      workManagerProxy.enqueueUniqueWork(getWorkId(widgetIds), ExistingWorkPolicy.KEEP, removeWidgetsWork)
    }
  }
}
