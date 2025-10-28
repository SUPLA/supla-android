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
    LocalizedStringId.CHANNEL_CAPTION_SEPTIC_TANK -> R.string.channel_caption_septic_tank
    LocalizedStringId.CHANNEL_CAPTION_WATER_TANK -> R.string.channel_caption_water_tank
    LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR -> R.string.channel_caption_container_level_sensor
    LocalizedStringId.CHANNEL_CAPTION_FLOOD_SENSOR -> R.string.channel_caption_flood_sensor
    LocalizedStringId.CHANNEL_CAPTION_UNKNOWN -> R.string.channel_not_supported
    LocalizedStringId.CHANNEL_CAPTION_MOTION_SENSOR -> R.string.channel_caption_motion_sensor
    LocalizedStringId.CHANNEL_CAPTION_BINARY_SENSOR -> R.string.channel_caption_binary_sensor

    LocalizedStringId.CHANNEL_STATUS_AWAITING -> R.string.channel_status_awaiting
    LocalizedStringId.CHANNEL_STATUS_UPDATING -> R.string.channel_status_updating
    LocalizedStringId.CHANNEL_STATUS_NOT_AVAILABLE -> R.string.channel_not_available

    LocalizedStringId.CHANNEL_BATTERY_LEVEL -> R.string.channel_battery_level

    LocalizedStringId.MOTOR_PROBLEM -> R.string.motor_problem
    LocalizedStringId.CALIBRATION_LOST -> R.string.calibration_lost
    LocalizedStringId.CALIBRATION_FAILED -> R.string.calibration_failed

    LocalizedStringId.OVERCURRENT_WARNING -> R.string.overcurrent_warning

    LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR -> R.string.thermostat_thermometer_error
    LocalizedStringId.THERMOSTAT_BATTER_COVER_OPEN -> R.string.thermostat_battery_cover_open
    LocalizedStringId.THERMOSTAT_CLOCK_ERROR -> R.string.thermostat_clock_error
    LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR -> R.string.thermostat_calibration_error

    LocalizedStringId.FLOOD_SENSOR_ACTIVE -> R.string.flooding_alarm_message
    LocalizedStringId.VALVE_MANUALLY_CLOSED -> R.string.valve_warning
    LocalizedStringId.VALVE_FLOODING -> R.string.valve_warning_flooding_short
    LocalizedStringId.VALVE_MOTOR_PROBLEM -> R.string.valve_warning_motor_problem
    LocalizedStringId.VALVE_SENSOR_OFFLINE -> R.string.valve_error_sensor_offline

    LocalizedStringId.CONTAINER_WARNING_LEVEL -> R.string.container_warning_level
    LocalizedStringId.CONTAINER_ALARM_LEVEL -> R.string.container_alarm_level
    LocalizedStringId.CONTAINER_INVALID_SENSOR_STATE -> R.string.container_invalid_sensor_state
    LocalizedStringId.CONTAINER_SOUND_ALARM -> R.string.container_sound_alarm

    // Add wizard
    LocalizedStringId.DEVICE_REGISTRATION_REQUEST_TIMEOUT -> R.string.device_reg_request_timeout
    LocalizedStringId.ENABLING_REGISTRATION_TIMEOUT -> R.string.enabling_registration_timeout
    LocalizedStringId.ADD_WIZARD_SCAN_TIMEOUT -> R.string.wizard_scan_timeout
    LocalizedStringId.ADD_WIZARD_DEVICE_NOT_FOUND -> R.string.wizard_iodevice_notfound
    LocalizedStringId.ADD_WIZARD_CONNECT_TIMEOUT -> R.string.wizard_connect_timeout
    LocalizedStringId.ADD_WIZARD_CONFIGURE_TIMEOUT -> R.string.wizard_configure_timeout
    LocalizedStringId.ADD_WIZARD_WIFI_ERROR -> R.string.wizard_wifi_error
    LocalizedStringId.ADD_WIZARD_RESULT_NOT_COMPATIBLE -> R.string.wizard_result_compat_error
    LocalizedStringId.ADD_WIZARD_RESULT_CONNECTION_ERROR -> R.string.wizard_result_conn_error
    LocalizedStringId.ADD_WIZARD_RESULT_FAILED -> R.string.wizard_result_failed
    LocalizedStringId.ADD_WIZARD_RECONNECT_TIMEOUT -> R.string.wizard_reconnect_timeout
    LocalizedStringId.ADD_WIZARD_DEVICE_TEMPORARILY_LOCKED -> R.string.add_wizard_device_temporarily_locked
    LocalizedStringId.ADD_WIZARD_STATE_PREPARING -> R.string.wizard_state_preparing
    LocalizedStringId.ADD_WIZARD_STATE_CONNECTING -> R.string.wizard_state_connecting
    LocalizedStringId.ADD_WIZARD_STATE_CONFIGURING -> R.string.wizard_state_configuring
    LocalizedStringId.ADD_WIZARD_STATE_FINISHING -> R.string.wizard_state_finishing

    LocalizedStringId.CHANNEL_STATE_UPTIME -> R.string.channel_state_uptime
    LocalizedStringId.CHANNEL_STATE_BATTERY_POWERED -> R.string.channel_state_battery_powered
    LocalizedStringId.CHANNEL_STATE_MAINS_POWERED -> R.string.channel_state_main_powered
    LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_UNKNOWN -> R.string.lastconnectionresetcause_unknown
    LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_ACTIVITY_TIMEOUT -> R.string.lastconnectionresetcause_activity_timeout
    LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_WIFI_CONNECTION_LOST -> R.string.lastconnectionresetcause_wifi_connection_lost
    LocalizedStringId.LAST_CONNECTION_RESET_CAUSE_SERVER_CONNECTION_LOST -> R.string.lastconnectionresetcause_server_connection_lost

    LocalizedStringId.RESULT_CODE_TEMPORARILY_UNAVAILABLE -> R.string.status_temporarily_unavailable
    LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED -> R.string.status_climit_exceded
    LocalizedStringId.RESULT_CODE_DEVICE_DISABLED -> R.string.status_device_disabled
    LocalizedStringId.RESULT_CODE_ACCESS_ID_DISABLED -> R.string.status_accessid_disabled
    LocalizedStringId.RESULT_CODE_REGISTRATION_DISABLED -> R.string.status_reg_disabled
    LocalizedStringId.RESULT_CODE_ACCESS_ID_NOT_ASSIGNED -> R.string.status_access_id_not_assigned
    LocalizedStringId.RESULT_CODE_INACTIVE -> R.string.status_accessid_inactive
    LocalizedStringId.RESULT_CODE_INCORRECT_EMAIL_OR_PASSWORD -> R.string.incorrect_email_or_password
    LocalizedStringId.RESULT_CODE_BAD_CREDENTIALS -> R.string.status_bad_credentials
    LocalizedStringId.RESULT_CODE_UNKNOWN_ERROR -> R.string.status_unknown_err

    LocalizedStringId.LIFESPAN_WARNING_REPLACE -> R.string.channel_uv_error
    LocalizedStringId.LIFESPAN_WARNING_SCHEDULE -> R.string.channel_uv_warning
    LocalizedStringId.LIFESPAN_WARNING -> R.string.channel_lightsource_warning
    LocalizedStringId.DIGIGLASS_PLANNED_REGENERATION -> R.string.dgf_planned_regeneration_in_progress
    LocalizedStringId.DIGIGLASS_REGENERATION_AFTER_20H -> R.string.dgf_regeneration_after20h
    LocalizedStringId.DIGIGLASS_TO_LONG_OPERATION -> R.string.dgf_too_long_operation_warning
  }
