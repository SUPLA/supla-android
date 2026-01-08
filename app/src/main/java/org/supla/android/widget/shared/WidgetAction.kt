package org.supla.android.widget.shared
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
import org.supla.android.lib.actions.ActionId
import org.supla.android.widget.shared.WidgetAction.AUTOMATIC_UPDATE
import org.supla.android.widget.shared.WidgetAction.BUTTON_PRESSED
import org.supla.android.widget.shared.WidgetAction.LEFT_BUTTON_PRESSED
import org.supla.android.widget.shared.WidgetAction.MANUAL_UPDATE
import org.supla.android.widget.shared.WidgetAction.REDRAW
import org.supla.android.widget.shared.WidgetAction.RIGHT_BUTTON_PRESSED
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue

enum class WidgetAction(val string: String) {
  /**
   * A
   */
  REDRAW("WidgetAction.REDRAW"),
  MANUAL_UPDATE("WidgetAction.MANUAL_UPDATE"),
  AUTOMATIC_UPDATE("WidgetAction.AUTOMATIC_UPDATE"),
  BUTTON_PRESSED("WidgetAction.BUTTON_PRESSED"),
  LEFT_BUTTON_PRESSED("WidgetAction.LEFT_BUTTON_PRESSED"),
  RIGHT_BUTTON_PRESSED("WidgetAction.RIGHT_BUTTON_PRESSED");

  companion object {
    fun from(string: String?): WidgetAction? {
      if (string == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
        return AUTOMATIC_UPDATE
      }

      return entries.firstOrNull { it.string == string }
    }

    fun getSingleButtonAction(function: SuplaFunction?): WidgetAction = hasAction(function).ifTrue { BUTTON_PRESSED } ?: MANUAL_UPDATE
  }
}

val WidgetAction?.isUpdate: Boolean
  get() = this == MANUAL_UPDATE || this == AUTOMATIC_UPDATE

fun WidgetAction.getActionId(function: SuplaFunction): ActionId? =
  when (this) {
    MANUAL_UPDATE, REDRAW, BUTTON_PRESSED, AUTOMATIC_UPDATE -> null
    LEFT_BUTTON_PRESSED -> getLeftButtonAction(function)
    RIGHT_BUTTON_PRESSED -> getRightButtonAction(function)
  }

private fun getLeftButtonAction(function: SuplaFunction): ActionId? =
  when (function) {
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
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE -> ActionId.OPEN

    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR -> ActionId.REVEAL

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
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> ActionId.TURN_OFF

    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN -> ActionId.COLLAPSE
  }

private fun getRightButtonAction(function: SuplaFunction): ActionId? =
  when (function) {
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
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE -> ActionId.CLOSE

    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR -> ActionId.SHUT

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
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> ActionId.TURN_ON

    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN -> ActionId.EXPAND
  }

private fun hasAction(function: SuplaFunction?): Boolean =
  when (function) {
    null,
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
    SuplaFunction.BINARY_SENSOR -> false

    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE,
    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.VERTICAL_BLIND,
    SuplaFunction.ROLLER_GARAGE_DOOR,
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
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.PROJECTOR_SCREEN,
    SuplaFunction.CURTAIN -> true
  }
