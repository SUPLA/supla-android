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

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.notifications.ON_OFF_WIDGET_NOTIFICATION_ID
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetCommandWorkerBase
import org.supla.android.widget.shared.getWorkId
import org.supla.core.shared.data.model.general.SuplaFunction

const val ARG_TURN_ON = "ARG_TURN_ON"

/**
 * Worker which is implemented for turning on/off switches. It supports following channel functions:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 * power switch [SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH]
 * staircase timer [SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER]
 * It supports also opening and closing of roller shutters
 */

@HiltWorker
class OnOffWidgetCommandWorker @AssistedInject constructor(
  loadChannelConfigUseCase: LoadChannelConfigUseCase,
  notificationsHelper: NotificationsHelper,
  vibrationHelper: VibrationHelper,
  appPreferences: ApplicationPreferences,
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters
) : WidgetCommandWorkerBase(loadChannelConfigUseCase, notificationsHelper, vibrationHelper, appPreferences, appContext, workerParams) {

  override val notificationId = ON_OFF_WIDGET_NOTIFICATION_ID

  override fun updateWidget(widgetId: Int) = updateOnOffWidget(applicationContext, widgetId)
  override fun valueWithUnit(): Boolean = true
  override fun perform(
    widgetId: Int,
    configuration: WidgetConfiguration
  ): Result = performCommon(
    widgetId,
    configuration,
    getAction(configuration)
  )

  companion object {
    fun enqueue(widgetIds: IntArray, turnOnOff: Boolean?, workManagerProxy: WorkManagerProxy) {
      val inputData = if (turnOnOff == null) {
        Data.Builder().putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds).build()
      } else {
        Data.Builder()
          .putBoolean(ARG_TURN_ON, turnOnOff)
          .putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
          .build()
      }

      val removeWidgetsWork = OneTimeWorkRequestBuilder<OnOffWidgetCommandWorker>()
        .setInputData(inputData)
        .build()

      // Work for widget ID is unique, so no other worker for the same ID will be started
      workManagerProxy.enqueueUniqueWork(getWorkId(widgetIds), ExistingWorkPolicy.KEEP, removeWidgetsWork)
    }
  }
}

private fun OnOffWidgetCommandWorker.getAction(configuration: WidgetConfiguration): ActionId? {
  if (!inputData.keyValueMap.keys.contains(ARG_TURN_ON)) {
    return null
  }

  val turnOn = inputData.getBoolean(ARG_TURN_ON, false)
  return when (configuration.subjectFunction) {
    SuplaFunction.OPEN_SENSOR_GATEWAY,
    SuplaFunction.OPEN_SENSOR_GATE,
    SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaFunction.OPEN_SENSOR_DOOR,
    SuplaFunction.NO_LIQUID_SENSOR,
    SuplaFunction.DEPTH_SENSOR,
    SuplaFunction.DISTANCE_SENSOR,
    SuplaFunction.OPENING_SENSOR_WINDOW,
    SuplaFunction.HOTEL_CARD_SENSOR,
    SuplaFunction.ALARM_ARMAMENT_SENSOR,
    SuplaFunction.MAIL_SENSOR,
    SuplaFunction.WIND_SENSOR,
    SuplaFunction.PRESSURE_SENSOR,
    SuplaFunction.RAIN_SENSOR,
    SuplaFunction.WEIGHT_SENSOR,
    SuplaFunction.WEATHER_STATION,
    SuplaFunction.THERMOMETER,
    SuplaFunction.HUMIDITY,
    SuplaFunction.HUMIDITY_AND_TEMPERATURE,
    SuplaFunction.UNKNOWN,
    SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaFunction.RING,
    SuplaFunction.ALARM,
    SuplaFunction.NOTIFICATION,
    SuplaFunction.ELECTRICITY_METER,
    SuplaFunction.IC_ELECTRICITY_METER,
    SuplaFunction.IC_GAS_METER,
    SuplaFunction.IC_WATER_METER,
    SuplaFunction.IC_HEAT_METER,
    SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaFunction.GENERAL_PURPOSE_METER,
    SuplaFunction.DIGIGLASS_HORIZONTAL,
    SuplaFunction.DIGIGLASS_VERTICAL,
    SuplaFunction.CONTAINER,
    SuplaFunction.SEPTIC_TANK,
    SuplaFunction.WATER_TANK,
    SuplaFunction.CONTAINER_LEVEL_SENSOR,
    SuplaFunction.FLOOD_SENSOR,
    SuplaFunction.PUMP_SWITCH,
    SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
    SuplaFunction.NONE,
    SuplaFunction.MOTION_SENSOR,
    SuplaFunction.BINARY_SENSOR -> null

    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> ActionId.OPEN
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> if (turnOn) ActionId.CLOSE else ActionId.OPEN

    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR -> if (turnOn) ActionId.SHUT else ActionId.REVEAL

    SuplaFunction.POWER_SWITCH,
    SuplaFunction.LIGHTSWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.DIMMER,
    SuplaFunction.DIMMER_CCT,
    SuplaFunction.RGB_LIGHTING,
    SuplaFunction.DIMMER_AND_RGB_LIGHTING,
    SuplaFunction.DIMMER_CCT_AND_RGB,
    SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> if (turnOn) ActionId.TURN_ON else ActionId.TURN_OFF

    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE -> if (turnOn) ActionId.CLOSE else ActionId.OPEN

    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN -> if (turnOn) ActionId.EXPAND else ActionId.COLLAPSE
  }
}
