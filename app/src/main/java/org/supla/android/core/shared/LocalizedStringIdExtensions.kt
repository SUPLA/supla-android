package org.supla.android.core.shared
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

import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedStringId

val LocalizedStringId.resourceId: Int
  get() = when (this) {
    LocalizedStringId.GENERAL_TURN_ON -> R.string.channel_btn_on
    LocalizedStringId.GENERAL_TURN_OFF -> R.string.channel_btn_off
    LocalizedStringId.GENERAL_OPEN -> R.string.channel_btn_open
    LocalizedStringId.GENERAL_CLOSE -> R.string.channel_btn_close
    LocalizedStringId.GENERAL_OPEN_CLOSE -> R.string.channel_btn_openclose
    LocalizedStringId.GENERAL_SHUT -> R.string.channel_btn_shut
    LocalizedStringId.GENERAL_REVEAL -> R.string.channel_btn_reveal
    LocalizedStringId.GENERAL_COLLAPSE -> R.string.channel_btn_collapse
    LocalizedStringId.GENERAL_EXPAND -> R.string.channel_btn_expand

    LocalizedStringId.GENERAL_YES -> R.string.yes
    LocalizedStringId.GENERAL_NO -> R.string.no

    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GATEWAY -> R.string.channel_caption_gatewayopeningsensor
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GATEWAY_LOCK -> R.string.channel_caption_gateway
    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GATE -> R.string.channel_caption_gateopeningsensor
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GATE -> R.string.channel_caption_gate
    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GARAGE_DOOR -> R.string.channel_caption_garagedooropeningsensor
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GARAGE_DOOR -> R.string.channel_caption_garagedoor
    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_DOOR -> R.string.channel_caption_dooropeningsensor
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_DOOR_LOCK -> R.string.channel_caption_door
    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_ROLLER_SHUTTER -> R.string.channel_caption_rsopeningsensor
    LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_ROOF_WINDOW -> R.string.channel_caption_roofwindowopeningsensor
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_ROLLER_SHUTTER -> R.string.channel_caption_rollershutter
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_ROOF_WINDOW -> R.string.channel_caption_roofwindow
    LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_FACADE_BLIND -> R.string.channel_caption_facade_blinds
    LocalizedStringId.CHANNEL_CAPTION_POWER_SWITCH -> R.string.channel_caption_powerswith
    LocalizedStringId.CHANNEL_CAPTION_LIGHTSWITCH -> R.string.channel_caption_lightswith
    LocalizedStringId.CHANNEL_CAPTION_THERMOMETER -> R.string.channel_caption_thermometer
    LocalizedStringId.CHANNEL_CAPTION_HUMIDITY -> R.string.channel_caption_humidity
    LocalizedStringId.CHANNEL_CAPTION_HUMIDITY_AND_TEMPERATURE -> R.string.channel_caption_humidityandtemperature
    LocalizedStringId.CHANNEL_CAPTION_WIND_SENSOR -> R.string.channel_caption_windsensor
    LocalizedStringId.CHANNEL_CAPTION_PRESSURE_SENSOR -> R.string.channel_caption_pressuresensor
    LocalizedStringId.CHANNEL_CAPTION_RAIN_SENSOR -> R.string.channel_caption_rainsensor
    LocalizedStringId.CHANNEL_CAPTION_WEIGHT_SENSOR -> R.string.channel_caption_weightsensor
    LocalizedStringId.CHANNEL_CAPTION_NO_LIQUID_SENSOR -> R.string.channel_caption_noliquidsensor
    LocalizedStringId.CHANNEL_CAPTION_DIMMER -> R.string.channel_caption_dimmer
    LocalizedStringId.CHANNEL_CAPTION_RGB_LIGHTING -> R.string.channel_caption_rgblighting
    LocalizedStringId.CHANNEL_CAPTION_DIMMER_AND_RGB_LIGHTING -> R.string.channel_caption_dimmerandrgblighting
    LocalizedStringId.CHANNEL_CAPTION_DEPTH_SENSOR -> R.string.channel_caption_depthsensor
    LocalizedStringId.CHANNEL_CAPTION_DISTANCE_SENSOR -> R.string.channel_caption_distancesensor
    LocalizedStringId.CHANNEL_CAPTION_OPENING_SENSOR_WINDOW -> R.string.channel_caption_windowopeningsensor
    LocalizedStringId.CHANNEL_CAPTION_HOTEL_CARD_SENSOR -> R.string.channel_caption_hotelcard
    LocalizedStringId.CHANNEL_CAPTION_ALARM_ARMAMENT_SENSOR -> R.string.channel_caption_alarm_armament
    LocalizedStringId.CHANNEL_CAPTION_MAIL_SENSOR -> R.string.channel_caption_mailsensor
    LocalizedStringId.CHANNEL_CAPTION_STAIRCASE_TIMER -> R.string.channel_caption_staircasetimer
    LocalizedStringId.CHANNEL_CAPTION_IC_GAS_METER -> R.string.channel_caption_gasmeter
    LocalizedStringId.CHANNEL_CAPTION_IC_WATER_METER -> R.string.channel_caption_watermeter
    LocalizedStringId.CHANNEL_CAPTION_IC_HEAT_METER -> R.string.channel_caption_heatmeter
    LocalizedStringId.CHANNEL_CAPTION_THERMOSTAT_HEATPOL_HOMEPLUS -> R.string.channel_caption_thermostat_hp_homeplus
    LocalizedStringId.CHANNEL_CAPTION_VALVE -> R.string.channel_caption_valve
    LocalizedStringId.CHANNEL_CAPTION_GENERAL_PURPOSE_MEASUREMENT -> R.string.channel_caption_general_purpose_measurement
    LocalizedStringId.CHANNEL_CAPTION_GENERAL_PURPOSE_METER -> R.string.channel_caption_general_purpose_meter
    LocalizedStringId.CHANNEL_CAPTION_THERMOSTAT -> R.string.channel_caption_thermostat
    LocalizedStringId.CHANNEL_CAPTION_ELECTRICITY_METER -> R.string.channel_caption_electricitymeter
    LocalizedStringId.CHANNEL_CAPTION_DIGIGLASS -> R.string.channel_caption_digiglass
    LocalizedStringId.CHANNEL_CAPTION_TERRACE_AWNING -> R.string.channel_caption_terrace_awning
    LocalizedStringId.CHANNEL_CAPTION_PROJECTOR_SCREEN -> R.string.channel_caption_projector_screen
    LocalizedStringId.CHANNEL_CAPTION_CURTAIN -> R.string.channel_caption_curtain
    LocalizedStringId.CHANNEL_CAPTION_VERTICAL_BLIND -> R.string.channel_caption_vertical_blinds
    LocalizedStringId.CHANNEL_CAPTION_ROLLER_GARAGE_DOOR -> R.string.channel_caption_roller_garage_door
    LocalizedStringId.CHANNEL_CAPTION_PUMP_SWITCH -> R.string.channel_caption_pump_switch
    LocalizedStringId.CHANNEL_CAPTION_HEAT_OR_COLD_SOURCE_SWITCH -> R.string.channel_caption_heat_or_cold_sourc_switch
    LocalizedStringId.CHANNEL_CAPTION_CONTAINER -> R.string.channel_caption_container
    LocalizedStringId.CHANNEL_CAPTION_UNKNOWN -> R.string.channel_not_supported

    LocalizedStringId.CHANNEL_BATTERY_LEVEL -> R.string.channel_battery_level

    LocalizedStringId.MOTOR_PROBLEM -> R.string.motor_problem
    LocalizedStringId.CALIBRATION_LOST -> R.string.calibration_lost
    LocalizedStringId.CALIBRATION_FAILED -> R.string.calibration_failed

    LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR -> R.string.thermostat_thermometer_error
    LocalizedStringId.THERMOSTAT_BATTER_COVER_OPEN -> R.string.thermostat_battery_cover_open
    LocalizedStringId.THERMOSTAT_CLOCK_ERROR -> R.string.thermostat_clock_error
  }
