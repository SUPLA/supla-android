package org.supla.android.lib;

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

import android.content.Context;
import org.supla.android.R;

@SuppressWarnings("unused")
public class SuplaConst {

  public static final int SUPLA_GUID_SIZE = 16;
  public static final int SUPLA_AUTHKEY_SIZE = 16;
  public static final int SUPLA_CLIENT_NAME_MAXSIZE = 201;
  public static final int SUPLA_ACCESSID_PWD_MAXSIZE = 33;
  public static final int SUPLA_SOFTVER_MAXSIZE = 21;
  public static final int SUPLA_EMAIL_MAXSIZE = 256;
  public static final int SUPLA_PASSWORD_MAXSIZE = 64;
  public static final int SUPLA_CHANNELVALUE_SIZE = 8;

  public static final int SUPLA_RESULT_RESPONSE_TIMEOUT = -8;
  public static final int SUPLA_RESULT_CANT_CONNECT_TO_HOST = -7;
  public static final int SUPLA_RESULT_HOST_NOT_FOUND = -6;
  public static final int SUPLA_RESULT_VERSION_ERROR = -1;

  // SUPLA_RESULTCODE_ are sent in TSuplaDataPacket.data
  public static final int SUPLA_RESULTCODE_FALSE = 2;
  public static final int SUPLA_RESULTCODE_TRUE = 3;
  public static final int SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE = 4;
  public static final int SUPLA_RESULTCODE_BAD_CREDENTIALS = 5;
  public static final int SUPLA_RESULTCODE_LOCATION_CONFLICT = 6;
  public static final int SUPLA_RESULTCODE_CHANNEL_CONFLICT = 7;
  public static final int SUPLA_RESULTCODE_DEVICE_DISABLED = 8;
  public static final int SUPLA_RESULTCODE_ACCESSID_DISABLED = 9;
  public static final int SUPLA_RESULTCODE_LOCATION_DISABLED = 10;
  public static final int SUPLA_RESULTCODE_CLIENT_DISABLED = 11;
  public static final int SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED = 12;
  public static final int SUPLA_RESULTCODE_DEVICE_LIMITEXCEEDED = 13;
  public static final int SUPLA_RESULTCODE_GUID_ERROR = 14;
  public static final int SUPLA_RESULTCODE_REGISTRATION_DISABLED = 17;
  public static final int SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED = 18;
  public static final int SUPLA_RESULTCODE_AUTHKEY_ERROR = 19;
  public static final int SUPLA_RESULTCODE_NO_LOCATION_AVAILABLE = 20;
  public static final int SUPLA_RESULTCODE_USER_CONFLICT = 21;
  public static final int SUPLA_RESULTCODE_UNAUTHORIZED = 22;
  public static final int SUPLA_RESULTCODE_AUTHORIZED = 23;
  public static final int SUPLA_RESULTCODE_NOT_ALLOWED = 24;
  public static final int SUPLA_RESULTCODE_CHANNELNOTFOUND = 25;
  public static final int SUPLA_RESULTCODE_UNKNOWN_ERROR = 26;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_BELONG_TO_GROUP = 27;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_HAS_SCHEDULE = 28;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_SCENE = 29;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_ACTION_TRIGGER = 30;
  public static final int SUPLA_RESULTCODE_ACCESSID_INACTIVE = 31;
  public static final int SUPLA_RESULTCODE_SUBJECT_NOT_FOUND = 34;
  public static final int SUPLA_RESULTCODE_INCORRECT_PARAMETERS = 35;
  public static final int SUPLA_RESULTCODE_CLIENT_NOT_EXISTS = 36;
  public static final int SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE = 38;
  public static final int SUPLA_RESULTCODE_NOT_REGISTERED = 40;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_VBT = 41;
  public static final int SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_PUSH = 42;
  public static final int SUPLA_CHANNELFNC_NONE = 0;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK = 10;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEGATE = 20;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR = 30;
  public static final int SUPLA_CHANNELFNC_THERMOMETER = 40;
  public static final int SUPLA_CHANNELFNC_HUMIDITY = 42;
  public static final int SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE = 45;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY = 50;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_GATE = 60;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR = 70;
  public static final int SUPLA_CHANNELFNC_NOLIQUIDSENSOR = 80;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK = 90;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_DOOR = 100;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER = 110;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW = 115;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER = 120;
  public static final int SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW = 125;
  public static final int SUPLA_CHANNELFNC_POWERSWITCH = 130;
  public static final int SUPLA_CHANNELFNC_LIGHTSWITCH = 140;
  public static final int SUPLA_CHANNELFNC_RING = 150;
  public static final int SUPLA_CHANNELFNC_ALARM = 160;
  public static final int SUPLA_CHANNELFNC_NOTIFICATION = 170;
  public static final int SUPLA_CHANNELFNC_DIMMER = 180;
  public static final int SUPLA_CHANNELFNC_RGBLIGHTING = 190;
  public static final int SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING = 200;
  public static final int SUPLA_CHANNELFNC_DEPTHSENSOR = 210;
  public static final int SUPLA_CHANNELFNC_DISTANCESENSOR = 220;
  public static final int SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW = 230;
  public static final int SUPLA_CHANNELFNC_HOTELCARDSENSOR = 235;
  public static final int SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR = 236;
  public static final int SUPLA_CHANNELFNC_MAILSENSOR = 240;
  public static final int SUPLA_CHANNELFNC_WINDSENSOR = 250;
  public static final int SUPLA_CHANNELFNC_PRESSURESENSOR = 260;
  public static final int SUPLA_CHANNELFNC_RAINSENSOR = 270;
  public static final int SUPLA_CHANNELFNC_WEIGHTSENSOR = 280;
  public static final int SUPLA_CHANNELFNC_WEATHER_STATION = 290;
  public static final int SUPLA_CHANNELFNC_STAIRCASETIMER = 300;
  public static final int SUPLA_CHANNELFNC_ELECTRICITY_METER = 310;
  public static final int SUPLA_CHANNELFNC_IC_ELECTRICITY_METER = 315;
  public static final int SUPLA_CHANNELFNC_IC_GAS_METER = 320;
  public static final int SUPLA_CHANNELFNC_IC_WATER_METER = 330;
  public static final int SUPLA_CHANNELFNC_IC_HEAT_METER = 340;
  public static final int SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS = 410;
  public static final int SUPLA_CHANNELFNC_HVAC_THERMOSTAT = 420;
  public static final int SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL = 422;
  //  public static final int SUPLA_CHANNELFNC_HVAC_DRYER = 423;
  //  public static final int SUPLA_CHANNELFNC_HVAC_FAN = 424;
  //  public static final int SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL = 425;
  public static final int SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER = 426;
  public static final int SUPLA_CHANNELFNC_VALVE_OPENCLOSE = 500;
  public static final int SUPLA_CHANNELFNC_VALVE_PERCENTAGE = 510;
  public static final int SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT = 520;
  public static final int SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER = 530;
  public static final int SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL = 800;
  public static final int SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL = 810;
  public static final int SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND = 900;
  public static final int SUPLA_CHANNELFNC_TERRACE_AWNING = 910;
  public static final int SUPLA_CHANNELFNC_PROJECTOR_SCREEN = 920;
  public static final int SUPLA_CHANNELFNC_CURTAIN = 930;
  public static final int SUPLA_CHANNELFNC_VERTICAL_BLIND = 940;
  public static final int SUPLA_CHANNELFNC_ROLLER_GARAGE_DOOR = 950;
  public static final int SUPLA_CHANNELFNC_PUMP_SWITCH = 960;
  public static final int SUPLA_CHANNELFNC_HEAT_OR_COLD_SOURCE_SWITCH = 970;

  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEGATEWAYLOCK = 0x0000001;
  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEGATE = 0x0000002;
  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEGARAGEDOOR = 0x000004;
  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEDOORLOCK = 0x0000008;
  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEROLLERSHUTTER = 0x0000010;
  public static final int SUPLA_BIT_FUNC_POWERSWITCH = 0x0000020;
  public static final int SUPLA_BIT_FUNC_LIGHTSWITCH = 0x0000040;
  public static final int SUPLA_BIT_FUNC_STAIRCASETIMER = 0x0000080;
  public static final int SUPLA_BIT_FUNC_THERMOMETER = 0x00000100;
  public static final int SUPLA_BIT_FUNC_HUMIDITYANDTEMPERATURE = 0x00000200;
  public static final int SUPLA_BIT_FUNC_HUMIDITY = 0x00000400;
  public static final int SUPLA_BIT_FUNC_WINDSENSOR = 0x00000800;
  public static final int SUPLA_BIT_FUNC_PRESSURESENSOR = 0x00001000;
  public static final int SUPLA_BIT_FUNC_RAINSENSOR = 0x00002000;
  public static final int SUPLA_BIT_FUNC_WEIGHTSENSOR = 0x00004000;
  public static final int SUPLA_BIT_FUNC_CONTROLLINGTHEROOFWINDOW = 0x00008000;

  public static final int SUPLA_CHANNELTYPE_IMPULSE_COUNTER = 5010;
  public static final int SUPLA_CHANNELTYPE_BRIDGE = 8000;

  public static final long SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE = 0x0001;
  public static final long SUPLA_CHANNEL_FLAG_IR_BRIDGE = 0x0002;
  public static final long SUPLA_CHANNEL_FLAG_RF_BRIDGE = 0x0004;

  public static final long SUPLA_CHANNEL_FLAG_CHART_TYPE_BAR = 0x0010;
  public static final long SUPLA_CHANNEL_FLAG_CHART_DS_TYPE_DIFFERENTAL = 0x0020;
  public static final long SUPLA_CHANNEL_FLAG_CHART_INTERPOLATE_MEASUREMENTS = 0x0040;

  public static final int SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK = 10;
  public static final int SUPLA_EVENT_CONTROLLINGTHEGATE = 20;
  public static final int SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR = 30;
  public static final int SUPLA_EVENT_CONTROLLINGTHEDOORLOCK = 40;
  public static final int SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER = 50;
  public static final int SUPLA_EVENT_CONTROLLINGTHEROOFWINDOW = 55;
  public static final int SUPLA_EVENT_POWERONOFF = 60;
  public static final int SUPLA_EVENT_LIGHTONOFF = 70;
  public static final int SUPLA_EVENT_VALVEOPENCLOSE = 90;
  public static final int SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED = 100;

  public static final int SUPLA_DCS_CALL_GETVERSION = 10;
  public static final int SUPLA_SDC_CALL_GETVERSION_RESULT = 20;
  public static final int SUPLA_SDC_CALL_VERSIONERROR = 30;
  public static final int SUPLA_DCS_CALL_PING_SERVER = 40;
  public static final int SUPLA_SDC_CALL_PING_SERVER_RESULT = 50;
  public static final int SUPLA_DS_CALL_REGISTER_DEVICE = 60;
  public static final int SUPLA_DS_CALL_REGISTER_DEVICE_B = 65;
  public static final int SUPLA_DS_CALL_REGISTER_DEVICE_C = 67;
  public static final int SUPLA_DS_CALL_REGISTER_DEVICE_D = 68;
  public static final int SUPLA_SD_CALL_REGISTER_DEVICE_RESULT = 70;
  public static final int SUPLA_CS_CALL_REGISTER_CLIENT = 80;
  public static final int SUPLA_CS_CALL_REGISTER_CLIENT_B = 85;
  public static final int SUPLA_CS_CALL_REGISTER_CLIENT_C = 86;
  public static final int SUPLA_SC_CALL_REGISTER_CLIENT_RESULT = 90;
  public static final int SUPLA_DS_CALL_DEVICE_CHANNEL_VALUE_CHANGED = 100;
  public static final int SUPLA_SD_CALL_CHANNEL_SET_VALUE = 110;
  public static final int SUPLA_DS_CALL_CHANNEL_SET_VALUE_RESULT = 120;
  public static final int SUPLA_SC_CALL_LOCATION_UPDATE = 130;
  public static final int SUPLA_SC_CALL_LOCATIONPACK_UPDATE = 140;
  public static final int SUPLA_SC_CALL_CHANNEL_UPDATE = 150;
  public static final int SUPLA_SC_CALL_CHANNELPACK_UPDATE = 160;
  public static final int SUPLA_SC_CALL_CHANNEL_VALUE_UPDATE = 170;
  public static final int SUPLA_CS_CALL_GET_NEXT = 180;
  public static final int SUPLA_SC_CALL_EVENT = 190;
  public static final int SUPLA_CS_CALL_CHANNEL_SET_VALUE = 200;
  public static final int SUPLA_CS_CALL_CHANNEL_SET_VALUE_B = 205;
  public static final int SUPLA_DCS_CALL_SET_ACTIVITY_TIMEOUT = 210;
  public static final int SUPLA_SDC_CALL_SET_ACTIVITY_TIMEOUT_RESULT = 220;
  public static final int SUPLA_DS_CALL_GET_FIRMWARE_UPDATE_URL = 300;
  public static final int SUPLA_SD_CALL_GET_FIRMWARE_UPDATE_URL_RESULT = 310;
  public static final int SUPLA_DCS_CALL_GET_REGISTRATION_ENABLED = 320;
  public static final int SUPLA_SDC_CALL_GET_REGISTRATION_ENABLED_RESULT = 330;
  public static final int SUPLA_CS_CALL_GET_OAUTH_PARAMETERS = 340;
  public static final int SUPLA_SC_CALL_GET_OAUTH_PARAMETERS_RESULT = 350;

  public static final int SUPLA_RESULT_CALL_NOT_ALLOWED = -5;
  public static final int SUPLA_RESULT_FALSE = 0;
  public static final int SUPLA_RESULT_TRUE = 1;

  public static final int EM_VAR_FREQ = 0x0001;
  public static final int EM_VAR_VOLTAGE = 0x0002;
  public static final int EM_VAR_CURRENT = 0x0004;
  public static final int EM_VAR_POWER_ACTIVE = 0x0008;
  public static final int EM_VAR_POWER_REACTIVE = 0x0010;
  public static final int EM_VAR_POWER_APPARENT = 0x0020;
  public static final int EM_VAR_POWER_FACTOR = 0x0040;
  public static final int EM_VAR_PHASE_ANGLE = 0x0080;
  public static final int EM_VAR_FORWARD_ACTIVE_ENERGY = 0x0100;
  public static final int EM_VAR_REVERSE_ACTIVE_ENERGY = 0x0200;
  public static final int EM_VAR_FORWARD_REACTIVE_ENERGY = 0x0400;
  public static final int EM_VAR_REVERSE_REACTIVE_ENERGY = 0x0800;
  public static final int EM_VAR_CURRENT_OVER_65A = 0x1000;
  public static final int EM_VAR_FORWARD_ACTIVE_ENERGY_BALANCED = 0x2000;
  public static final int EM_VAR_REVERSE_ACTIVE_ENERGY_BALANCED = 0x4000;
  public static final int EM_VAR_VOLTAGE_PHASE_ANGLE_12 = 0x10000;
  public static final int EM_VAR_VOLTAGE_PHASE_ANGLE_13 = 0x20000;
  public static final int EM_VAR_VOLTAGE_PHASE_SEQUENCE = 0x40000;
  public static final int EM_VAR_CURRENT_PHASE_SEQUENCE = 0x80000;
  public static final int EM_VAR_ALL = 0xFFFF;

  public static final int EM_PHASE_SEQUENCE_VOLTAGE = 0x01;
  public static final int EM_PHASE_SEQUENCE_CURRENT = 0x02;

  public static final int EM_VAR_POWER_ACTIVE_KW = 0x100000;
  public static final int EM_VAR_POWER_REACTIVE_KVAR = 0x200000;
  public static final int EM_VAR_POWER_APPARENT_KVA = 0x400000;

  public static final int EV_TYPE_ELECTRICITY_METER_MEASUREMENT_V1 = 10;
  public static final int EV_TYPE_ELECTRICITY_METER_MEASUREMENT_V2 = 12;
  public static final int EV_TYPE_IMPULSE_COUNTER_DETAILS_V1 = 20;
  public static final int EV_TYPE_THERMOSTAT_DETAILS_V1 = 30;
  public static final int EV_TYPE_CHANNEL_STATE_V1 = 40;
  public static final int EV_TYPE_TIMER_STATE_V1 = 50;
  public static final int EV_TYPE_CHANNEL_AND_TIMER_STATE_V1 = 60;

  public static final int SUPLA_THERMOSTAT_CMD_TURNON = 1;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_AUTO = 2;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_COOL = 3;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_HEAT = 4;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_NORMAL = 5;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_ECO = 6;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO = 7;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_DRY = 8;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_FANONY = 9;
  public static final int SUPLA_THERMOSTAT_CMD_SET_MODE_PURIFIER = 10;
  public static final int SUPLA_THERMOSTAT_CMD_SET_SCHEDULE = 11;
  public static final int SUPLA_THERMOSTAT_CMD_SET_TIME = 12;
  public static final int SUPLA_THERMOSTAT_CMD_SET_TEMPERATURE = 13;

  public static final int SUPLA_MFR_UNKNOWN = 0;
  public static final int SUPLA_MFR_ACSOFTWARE = 1;
  public static final int SUPLA_MFR_TRANSCOM = 2;
  public static final int SUPLA_MFR_LOGI = 3;
  public static final int SUPLA_MFR_ZAMEL = 4;
  public static final int SUPLA_MFR_NICE = 5;
  public static final int SUPLA_MFR_ITEAD = 6;
  public static final int SUPLA_MFR_DOYLETRATT = 7;
  public static final int SUPLA_MFR_HEATPOL = 8;
  public static final int SUPLA_MFR_FAKRO = 9;
  public static final int SUPLA_MFR_PEVEKO = 10;
  public static final int SUPLA_MFR_WEKTA = 11;
  public static final int SUPLA_MFR_STA_SYSTEM = 12;
  public static final int SUPLA_MFR_DGF = 13;
  public static final int SUPLA_MFR_COMELIT = 14;

  public static final int ZAM_PRODID_DIW_01 = 2000;
  public static final int COM_PRODID_WDIM100 = 2000;

  public static final int SUPLA_VALVE_FLAG_FLOODING = 0x1;
  public static final int SUPLA_VALVE_FLAG_MANUALLY_CLOSED = 0x2;

  public static final int ZWAVE_NODE_FLAG_CHANNEL_ASSIGNED = 0x1;
  public static final int ZWAVE_NODE_FLAG_WAKEUP_TIME_SETTABLE = 0x2;

  public static final int SUPLA_CALCFG_RESULT_FALSE = 0;
  public static final int SUPLA_CALCFG_RESULT_TRUE = 1;
  public static final int SUPLA_CALCFG_RESULT_DONE = 2;
  public static final int SUPLA_CALCFG_RESULT_IN_PROGRESS = 3;
  public static final int SUPLA_CALCFG_RESULT_NODE_FOUND = 4;
  public static final int SUPLA_CALCFG_RESULT_SENDER_CONFLICT = 100;
  public static final int SUPLA_CALCFG_RESULT_TIMEOUT = 101;
  public static final int SUPLA_CALCFG_RESULT_NOT_SUPPORTED = 102;
  public static final int SUPLA_CALCFG_RESULT_ID_NOT_EXISTS = 103;
  public static final int SUPLA_CALCFG_RESULT_UNAUTHORIZED = 104;
  public static final int SUPLA_CALCFG_RESULT_DEBUG = 105;
  public static final int SUPLA_CALCFG_RESULT_NOT_SUPPORTED_IN_SLAVE_MODE = 106;

  public static final int SUPLA_RELAY_FLAG_OVERCURRENT_RELAY_OFF = 0x1;

  public static final int RS_VALUE_FLAG_TILT_IS_SET = 0x1;
  public static final int RS_VALUE_FLAG_CALIBRATION_FAILED = 0x2;
  public static final int RS_VALUE_FLAG_CALIBRATION_LOST = 0x4;
  public static final int RS_VALUE_FLAG_MOTOR_PROBLEM = 0x8;
  public static final int RS_VALUE_FLAG_CALIBRATION_IN_PROGRESS = 0x10;

  public static final int SUPLA_CTR_ROLLER_SHUTTER_OPEN = 10;
  public static final int SUPLA_CTR_ROLLER_SHUTTER_CLOSE = 110;

  public static String getFunctionName(int func, Context context) {
    int resId = -1;

    switch (func) {
      case SUPLA_CHANNELFNC_NONE:
        resId = R.string.channel_func_none;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
        resId = R.string.channel_func_controllingthegatewaylock;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
        resId = R.string.channel_func_controllingthegate;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
        resId = R.string.channel_func_controllingthegaragedoor;
        break;
      case SUPLA_CHANNELFNC_THERMOMETER:
        resId = R.string.channel_func_thermometer;
        break;
      case SUPLA_CHANNELFNC_HUMIDITY:
        resId = R.string.channel_func_humidity;
        break;
      case SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
        resId = R.string.channel_func_humidityandtemperature;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
        resId = R.string.channel_func_opensensor_gateway;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_GATE:
        resId = R.string.channel_func_opensensor_gate;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
        resId = R.string.channel_func_opensensor_garagedoor;
        break;
      case SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
        resId = R.string.channel_func_noliquidsensor;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
        resId = R.string.channel_func_controllingthedoorlock;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
        resId = R.string.channel_func_opensensor_door;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        resId = R.string.channel_func_controllingtherollershutter;
        break;
      case SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
        resId = R.string.channel_func_controllingtheroofwindow;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
        resId = R.string.channel_func_opensensor_rollershutter;
        break;
      case SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW:
        resId = R.string.channel_func_opensensor_roofwindow;
        break;
      case SUPLA_CHANNELFNC_POWERSWITCH:
        resId = R.string.channel_func_powerswitch;
        break;
      case SUPLA_CHANNELFNC_LIGHTSWITCH:
        resId = R.string.channel_func_lightswitch;
        break;
      case SUPLA_CHANNELFNC_DIMMER:
        resId = R.string.channel_func_dimmer;
        break;
      case SUPLA_CHANNELFNC_RGBLIGHTING:
        resId = R.string.channel_func_rgblighting;
        break;
      case SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        resId = R.string.channel_func_dimmerandrgblighting;
        break;
      case SUPLA_CHANNELFNC_DEPTHSENSOR:
        resId = R.string.channel_func_depthsensor;
        break;
      case SUPLA_CHANNELFNC_DISTANCESENSOR:
        resId = R.string.channel_func_distancesensor;
        break;
      case SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
        resId = R.string.channel_func_openingsensor_window;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR:
        resId = R.string.channel_caption_hotelcard;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR:
        resId = R.string.channel_caption_alarm_armament;
        break;
      case SUPLA_CHANNELFNC_MAILSENSOR:
        resId = R.string.channel_func_mailsensor;
        break;
      case SUPLA_CHANNELFNC_WINDSENSOR:
        resId = R.string.channel_func_windsensor;
        break;
      case SUPLA_CHANNELFNC_PRESSURESENSOR:
        resId = R.string.channel_func_pressuresensor;
        break;
      case SUPLA_CHANNELFNC_RAINSENSOR:
        resId = R.string.channel_func_rainsensor;
        break;
      case SUPLA_CHANNELFNC_WEIGHTSENSOR:
        resId = R.string.channel_func_weightsensor;
        break;
      case SUPLA_CHANNELFNC_WEATHER_STATION:
        resId = R.string.channel_func_weather_station;
        break;
      case SUPLA_CHANNELFNC_STAIRCASETIMER:
        resId = R.string.channel_func_staircasetimer;
        break;
      case SUPLA_CHANNELFNC_ELECTRICITY_METER:
      case SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
        resId = R.string.channel_func_electricity_meter;
        break;
      case SUPLA_CHANNELFNC_IC_GAS_METER:
        resId = R.string.channel_func_gas_meter;
        break;
      case SUPLA_CHANNELFNC_IC_WATER_METER:
        resId = R.string.channel_func_water_meter;
        break;
      case SUPLA_CHANNELFNC_IC_HEAT_METER:
        resId = R.string.channel_func_heat_meter;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL:
      case SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER:
        resId = R.string.channel_func_thermostat;
        break;
      case SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        resId = R.string.channel_func_thermostat_heatpol_homeplus;
        break;
      case SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        resId = R.string.channel_func_valve_openclose;
        break;
      case SUPLA_CHANNELFNC_VALVE_PERCENTAGE:
        resId = R.string.channel_func_valve_percentage;
        break;
      case SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
      case SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
        resId = R.string.channel_func_digiglass;
        break;
    }

    return resId == -1 ? Integer.toString(func) : context.getResources().getString(resId);
  }

  public static int functionBit2functionNumber(int bit) {

    if (bit != 0) {
      switch (bit) {
        case SUPLA_BIT_FUNC_CONTROLLINGTHEGATEWAYLOCK:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK;
        case SUPLA_BIT_FUNC_CONTROLLINGTHEGATE:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEGATE;
        case SUPLA_BIT_FUNC_CONTROLLINGTHEGARAGEDOOR:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR;
        case SUPLA_BIT_FUNC_CONTROLLINGTHEDOORLOCK:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK;
        case SUPLA_BIT_FUNC_CONTROLLINGTHEROLLERSHUTTER:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER;
        case SUPLA_BIT_FUNC_CONTROLLINGTHEROOFWINDOW:
          return SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW;
        case SUPLA_BIT_FUNC_POWERSWITCH:
          return SUPLA_CHANNELFNC_POWERSWITCH;
        case SUPLA_BIT_FUNC_LIGHTSWITCH:
          return SUPLA_CHANNELFNC_LIGHTSWITCH;
        case SUPLA_BIT_FUNC_STAIRCASETIMER:
          return SUPLA_CHANNELFNC_STAIRCASETIMER;
        case SUPLA_BIT_FUNC_THERMOMETER:
          return SUPLA_CHANNELFNC_THERMOMETER;
        case SUPLA_BIT_FUNC_HUMIDITYANDTEMPERATURE:
          return SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE;
        case SUPLA_BIT_FUNC_HUMIDITY:
          return SUPLA_CHANNELFNC_HUMIDITY;
        case SUPLA_BIT_FUNC_WINDSENSOR:
          return SUPLA_CHANNELFNC_WINDSENSOR;
        case SUPLA_BIT_FUNC_PRESSURESENSOR:
          return SUPLA_CHANNELFNC_PRESSURESENSOR;
        case SUPLA_BIT_FUNC_RAINSENSOR:
          return SUPLA_CHANNELFNC_RAINSENSOR;
        case SUPLA_BIT_FUNC_WEIGHTSENSOR:
          return SUPLA_CHANNELFNC_WEIGHTSENSOR;
      }
    }

    return 0;
  }
}
