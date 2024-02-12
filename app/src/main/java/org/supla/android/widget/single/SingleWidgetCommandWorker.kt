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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Preferences
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.notifications.SINGLE_WIDGET_NOTIFICATION_ID
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaConst.*
import org.supla.android.lib.actions.ActionId
import org.supla.android.usecases.channel.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetCommandWorkerBase
import org.supla.android.widget.shared.configuration.ItemType
import org.supla.android.widget.shared.configuration.WidgetAction

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
  notificationsHelper: NotificationsHelper,
  loadChannelConfigUseCase: LoadChannelConfigUseCase,
  appPreferences: Preferences,
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters
) : WidgetCommandWorkerBase(notificationsHelper, loadChannelConfigUseCase, appPreferences, appContext, workerParams) {

  override val notificationId = SINGLE_WIDGET_NOTIFICATION_ID

  override fun updateWidget(widgetId: Int) = updateSingleWidget(applicationContext, widgetId)
  override fun valueWithUnit(): Boolean = false

  override fun perform(widgetId: Int, configuration: WidgetConfiguration): Result {
    val action = WidgetAction.fromId(configuration.actionId)
    if (configuration.itemType == ItemType.SCENE) {
      if (action != null) {
        callAction(configuration, action.suplaAction)
      }
    } else {
      when (configuration.itemFunction) {
        SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR -> if (action != null) {
          callAction(configuration, action.suplaAction)
        }
        SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK,
        SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK -> callAction(configuration, ActionId.OPEN)
        SUPLA_CHANNELFNC_LIGHTSWITCH,
        SUPLA_CHANNELFNC_POWERSWITCH,
        SUPLA_CHANNELFNC_STAIRCASETIMER -> {
          if (action == null) {
            return callCommon(widgetId, configuration)
          }
          callAction(configuration, action.suplaAction)
        }
        else -> return callCommon(widgetId, configuration)
      }
    }
    return Result.success()
  }

  private fun callCommon(widgetId: Int, configuration: WidgetConfiguration): Result {
    val turnOnOrClose = configuration.actionId == WidgetAction.TURN_ON.actionId ||
      configuration.actionId == WidgetAction.MOVE_DOWN.actionId
    return performCommon(widgetId, configuration, turnOnOrClose)
  }
}
