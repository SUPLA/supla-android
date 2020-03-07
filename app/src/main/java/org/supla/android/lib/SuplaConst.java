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

    public final static int SUPLA_GUID_SIZE = 16;
    public final static int SUPLA_AUTHKEY_SIZE = 16;
    public final static int SUPLA_CLIENT_NAME_MAXSIZE = 201;
    public final static int SUPLA_ACCESSID_PWD_MAXSIZE = 33;
    public final static int SUPLA_SOFTVER_MAXSIZE = 21;
    public final static int SUPLA_EMAIL_MAXSIZE = 256;
    public final static int SUPLA_PASSWORD_MAXSIZE = 64;
    public final static int SUPLA_CHANNELVALUE_SIZE = 8;
    public final static int SUPLA_RESULTCODE_NONE                      = 0;
    public final static int SUPLA_RESULTCODE_UNSUPORTED                = 1;
    public final static int SUPLA_RESULTCODE_FALSE                     = 2;
    public final static int SUPLA_RESULTCODE_TRUE                      = 3;
    public final static int SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE   = 4;
    public final static int SUPLA_RESULTCODE_BAD_CREDENTIALS           = 5;
    public final static int SUPLA_RESULTCODE_LOCATION_CONFLICT         = 6;
    public final static int SUPLA_RESULTCODE_CHANNEL_CONFLICT          = 7;
    public final static int SUPLA_RESULTCODE_DEVICE_DISABLED           = 8;
    public final static int SUPLA_RESULTCODE_ACCESSID_DISABLED         = 9;
    public final static int SUPLA_RESULTCODE_LOCATION_DISABLED         = 10;
    public final static int SUPLA_RESULTCODE_CLIENT_DISABLED           = 11;
    public final static int SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED      = 12;
    public final static int SUPLA_RESULTCODE_DEVICE_LIMITEXCEEDED      = 13;
    public final static int SUPLA_RESULTCODE_GUID_ERROR                = 14;
    public final static int SUPLA_RESULTCODE_HOSTNOTFOUND              = 15;
    public final static int SUPLA_RESULTCODE_CANTCONNECTTOHOST         = 16;
    public final static int SUPLA_RESULTCODE_REGISTRATION_DISABLED     = 17;
    public final static int SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED     = 18;
    public final static int SUPLA_RESULTCODE_AUTHKEY_ERROR             = 19;
    public final static int SUPLA_RESULTCODE_NO_LOCATION_AVAILABLE     = 20;
    public final static int SUPLA_RESULTCODE_USER_CONFLICT             = 21;
    public final static int SUPLA_RESULTCODE_UNAUTHORIZED              = 22;
    public final static int SUPLA_RESULTCODE_AUTHORIZED                = 23;

    public final static int SUPLA_CHANNELFNC_NONE                          =  0;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK     = 10;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGATE            = 20;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR      = 30;
    public final static int SUPLA_CHANNELFNC_THERMOMETER                   = 40;
    public final static int SUPLA_CHANNELFNC_HUMIDITY                      = 42;
    public final static int SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE        = 45;
    public final static int SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY            = 50;
    public final static int SUPLA_CHANNELFNC_OPENSENSOR_GATE               = 60;
    public final static int SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR         = 70;
    public final static int SUPLA_CHANNELFNC_NOLIQUIDSENSOR                = 80;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK        = 90;
    public final static int SUPLA_CHANNELFNC_OPENSENSOR_DOOR               = 100;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER   = 110;
    public final static int SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER      = 120;
    public final static int SUPLA_CHANNELFNC_POWERSWITCH                   = 130;
    public final static int SUPLA_CHANNELFNC_LIGHTSWITCH                   = 140;
    public final static int SUPLA_CHANNELFNC_RING                          = 150;
    public final static int SUPLA_CHANNELFNC_ALARM                         = 160;
    public final static int SUPLA_CHANNELFNC_NOTIFICATION                  = 170;
    public final static int SUPLA_CHANNELFNC_DIMMER                        = 180;
    public final static int SUPLA_CHANNELFNC_RGBLIGHTING                   = 190;
    public final static int SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING          = 200;
    public final static int SUPLA_CHANNELFNC_DEPTHSENSOR                   = 210;
    public final static int SUPLA_CHANNELFNC_DISTANCESENSOR                = 220;
    public final static int SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW          = 230;
    public final static int SUPLA_CHANNELFNC_MAILSENSOR                    = 240;
    public final static int SUPLA_CHANNELFNC_WINDSENSOR                    = 250;
    public final static int SUPLA_CHANNELFNC_PRESSURESENSOR                = 260;
    public final static int SUPLA_CHANNELFNC_RAINSENSOR                    = 270;
    public final static int SUPLA_CHANNELFNC_WEIGHTSENSOR                  = 280;
    public final static int SUPLA_CHANNELFNC_WEATHER_STATION               = 290;
    public final static int SUPLA_CHANNELFNC_STAIRCASETIMER                = 300;
    public final static int SUPLA_CHANNELFNC_ELECTRICITY_METER             = 310;
    public final static int SUPLA_CHANNELFNC_IC_ELECTRICITY_METER          = 315;
    public final static int SUPLA_CHANNELFNC_IC_GAS_METER                  = 320;
    public final static int SUPLA_CHANNELFNC_IC_WATER_METER                = 330;
    public final static int SUPLA_CHANNELFNC_IC_HEAT_METER                 = 340;
    public final static int SUPLA_CHANNELFNC_THERMOSTAT                    = 400;
    public final static int SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS   = 410;
    public final static int SUPLA_CHANNELFNC_VALVE_OPENCLOSE               = 500;
    public final static int SUPLA_CHANNELFNC_VALVE_PERCENTAGE              = 510;


    public final static int SUPLA_BIT_FUNC_CONTROLLINGTHEGATEWAYLOCK = 0x0001;
    public final static int SUPLA_BIT_FUNC_CONTROLLINGTHEGATE = 0x0002;
    public final static int SUPLA_BIT_FUNC_CONTROLLINGTHEGARAGEDOOR = 0x0004;
    public final static int SUPLA_BIT_FUNC_CONTROLLINGTHEDOORLOCK = 0x0008;
    public final static int SUPLA_BIT_FUNC_CONTROLLINGTHEROLLERSHUTTER = 0x0010;
    public final static int SUPLA_BIT_FUNC_POWERSWITCH = 0x0020;
    public final static int SUPLA_BIT_FUNC_LIGHTSWITCH = 0x0040;
    public final static int SUPLA_BIT_FUNC_STAIRCASETIMER = 0x0080;

    public final static int SUPLA_CHANNELTYPE_IMPULSE_COUNTER = 5010;
    public final static int SUPLA_CHANNELTYPE_BRIDGE = 8000;

    public final static int SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE = 0x0001;
    public final static int SUPLA_CHANNEL_FLAG_IR_BRIDGE = 0x0002;
    public final static int SUPLA_CHANNEL_FLAG_RF_BRIDGE = 0x0004;
    public final static int SUPLA_CHANNEL_FLAG_DETAILED_STATUS = 0x0008;
    public final static int SUPLA_CHANNEL_FLAG_CHART_TYPE_BAR = 0x0010;
    public final static int SUPLA_CHANNEL_FLAG_CHART_DS_TYPE_DIFFERENTAL = 0x0020;
    public final static int SUPLA_CHANNEL_FLAG_CHART_INTERPOLATE_MEASUREMENTS = 0x0040;
    public final static int SUPLA_CHANNEL_FLAG_CAP_ACTION1 = 0x0080;
    public final static int SUPLA_CHANNEL_FLAG_CAP_ACTION2 = 0x0100;
    public final static int SUPLA_CHANNEL_FLAG_CAP_ACTION3 = 0x0200;
    public final static int SUPLA_CHANNEL_FLAG_CAP_ACTION4 = 0x0400;
    public final static int SUPLA_CHANNEL_FLAG_CAP_ACTION5 = 0x0800;
    public final static int SUPLA_CHANNEL_FLAG_CHANNELSTATE = 0x00010000;
    public final static int SUPLA_CHANNEL_FLAG_PHASE1_UNSUPPORTED = 0x00020000;
    public final static int SUPLA_CHANNEL_FLAG_PHASE2_UNSUPPORTED = 0x00040000;
    public final static int SUPLA_CHANNEL_FLAG_PHASE3_UNSUPPORTED = 0x00080000;
    public final static int SUPLA_CHANNEL_FLAG_RS_AUTOCALIBRATION = 0x00100000;
    public final static int SUPLA_CHANNEL_FLAG_RSA_ENCRYPTED_PIN_REQUIRED = 0x00200000;

    public final static int SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK          = 10;
    public final static int SUPLA_EVENT_CONTROLLINGTHEGATE                 = 20;
    public final static int SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR           = 30;
    public final static int SUPLA_EVENT_CONTROLLINGTHEDOORLOCK             = 40;
    public final static int SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER        = 50;
    public final static int SUPLA_EVENT_POWERONOFF                         = 60;
    public final static int SUPLA_EVENT_LIGHTONOFF                         = 70;


    public final static int SUPLA_DCS_CALL_GETVERSION                      =   10;
    public final static int SUPLA_SDC_CALL_GETVERSION_RESULT               =   20;
    public final static int SUPLA_SDC_CALL_VERSIONERROR                    =   30;
    public final static int SUPLA_DCS_CALL_PING_SERVER                     =   40;
    public final static int SUPLA_SDC_CALL_PING_SERVER_RESULT              =   50;
    public final static int SUPLA_DS_CALL_REGISTER_DEVICE                  =   60;
    public final static int SUPLA_DS_CALL_REGISTER_DEVICE_B                =   65;
    public final static int SUPLA_DS_CALL_REGISTER_DEVICE_C                =   67;
    public final static int SUPLA_DS_CALL_REGISTER_DEVICE_D                =   68;
    public final static int SUPLA_SD_CALL_REGISTER_DEVICE_RESULT           =   70;
    public final static int SUPLA_CS_CALL_REGISTER_CLIENT                  =   80;
    public final static int SUPLA_CS_CALL_REGISTER_CLIENT_B                =   85;
    public final static int SUPLA_CS_CALL_REGISTER_CLIENT_C                =   86;
    public final static int SUPLA_SC_CALL_REGISTER_CLIENT_RESULT           =   90;
    public final static int SUPLA_DS_CALL_DEVICE_CHANNEL_VALUE_CHANGED     =   100;
    public final static int SUPLA_SD_CALL_CHANNEL_SET_VALUE                =   110;
    public final static int SUPLA_DS_CALL_CHANNEL_SET_VALUE_RESULT         =   120;
    public final static int SUPLA_SC_CALL_LOCATION_UPDATE                  =   130;
    public final static int SUPLA_SC_CALL_LOCATIONPACK_UPDATE              =   140;
    public final static int SUPLA_SC_CALL_CHANNEL_UPDATE                   =   150;
    public final static int SUPLA_SC_CALL_CHANNELPACK_UPDATE               =   160;
    public final static int SUPLA_SC_CALL_CHANNEL_VALUE_UPDATE             =   170;
    public final static int SUPLA_CS_CALL_GET_NEXT                         =   180;
    public final static int SUPLA_SC_CALL_EVENT                            =   190;
    public final static int SUPLA_CS_CALL_CHANNEL_SET_VALUE                =   200;
    public final static int SUPLA_CS_CALL_CHANNEL_SET_VALUE_B              =   205;
    public final static int SUPLA_DCS_CALL_SET_ACTIVITY_TIMEOUT            =   210;
    public final static int SUPLA_SDC_CALL_SET_ACTIVITY_TIMEOUT_RESULT     =   220;
    public final static int SUPLA_DS_CALL_GET_FIRMWARE_UPDATE_URL          =   300;
    public final static int SUPLA_SD_CALL_GET_FIRMWARE_UPDATE_URL_RESULT   =   310;
    public final static int SUPLA_DCS_CALL_GET_REGISTRATION_ENABLED        =   320;
    public final static int SUPLA_SDC_CALL_GET_REGISTRATION_ENABLED_RESULT =   330;
    public final static int SUPLA_CS_CALL_GET_OAUTH_PARAMETERS              =  340;
    public final static int SUPLA_SC_CALL_GET_OAUTH_PARAMETERS_RESULT       =  350;


    public final static int SUPLA_RESULT_CALL_NOT_ALLOWED     =  -5;
    public final static int SUPLA_RESULT_FALSE                =   0;
    public final static int SUPLA_RESULT_TRUE                 =   1;


    public final static int EM_VAR_FREQ = 0x0001;
    public final static int EM_VAR_VOLTAGE = 0x0002;
    public final static int EM_VAR_CURRENT = 0x0004;
    public final static int EM_VAR_POWER_ACTIVE = 0x0008;
    public final static int EM_VAR_POWER_REACTIVE = 0x0010;
    public final static int EM_VAR_POWER_APPARENT = 0x0020;
    public final static int EM_VAR_POWER_FACTOR = 0x0040;
    public final static int EM_VAR_PHASE_ANGLE = 0x0080;
    public final static int EM_VAR_FORWARD_ACTIVE_ENERGY = 0x0100;
    public final static int EM_VAR_REVERSE_ACTIVE_ENERGY = 0x0200;
    public final static int EM_VAR_FORWARD_REACTIVE_ENERGY = 0x0400;
    public final static int EM_VAR_REVERSE_REACTIVE_ENERGY = 0x0800;
    public final static int EM_VAR_CURRENT_OVER_65A = 0x1000;
    public final static int EM_VAR_ALL = 0xFFFF;

    public final static int EV_TYPE_ELECTRICITY_METER_MEASUREMENT_V1 = 10;
    public final static int EV_TYPE_IMPULSE_COUNTER_DETAILS_V1 = 20;
    public final static int EV_TYPE_THERMOSTAT_DETAILS_V1 = 30;
    public final static int EV_TYPE_CHANNEL_STATE_V1 = 40;

    public final static int SUPLA_THERMOSTAT_CMD_TURNON = 1;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_AUTO = 2;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_COOL = 3;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_HEAT = 4;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_NORMAL = 5;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_ECO = 6;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_TURBO = 7;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_DRY = 8;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_FANONY = 9;
    public final static int SUPLA_THERMOSTAT_CMD_SET_MODE_PURIFIER = 10;
    public final static int SUPLA_THERMOSTAT_CMD_SET_SCHEDULE = 11;
    public final static int SUPLA_THERMOSTAT_CMD_SET_TIME = 12;
    public final static int SUPLA_THERMOSTAT_CMD_SET_TEMPERATURE = 13;

    public final static int SUPLA_MFR_UNKNOWN = 0;
    public final static int SUPLA_MFR_ACSOFTWARE = 1;
    public final static int SUPLA_MFR_TRANSCOM = 2;
    public final static int SUPLA_MFR_LOGI = 3;
    public final static int SUPLA_MFR_ZAMEL = 4;
    public final static int SUPLA_MFR_NICE = 5;
    public final static int SUPLA_MFR_ITEAD = 6;
    public final static int SUPLA_MFR_DOYLETRATT = 7;
    public final static int SUPLA_MFR_HEATPOL = 8;

    public final static int SUPLA_VALVE_FLAG_FLOODING = 0x1;
    public final static int SUPLA_VALVE_FLAG_MANUALLY_CLOSED = 0x2;


    public static String getFunctionName(int func, Context context) {
        int idx = -1;

        switch (func) {
            case SuplaConst.SUPLA_CHANNELFNC_NONE:
                idx = R.string.channel_func_none;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                idx = R.string.channel_func_gatewayopeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
                idx = R.string.channel_func_gateway;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                idx = R.string.channel_func_gateopeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                idx = R.string.channel_func_gate;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                idx = R.string.channel_func_garagedooropeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                idx = R.string.channel_func_garagedoor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                idx = R.string.channel_func_dooropeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                idx = R.string.channel_func_door;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
                idx = R.string.channel_func_rsopeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                idx = R.string.channel_func_rollershutter;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                idx = R.string.channel_func_powerswith;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                idx = R.string.channel_func_lightswith;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                idx = R.string.channel_func_thermometer;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
                idx = R.string.channel_func_humidity;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                idx = R.string.channel_func_humidityandtemperature;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR:
                idx = R.string.channel_func_windsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR:
                idx = R.string.channel_func_pressuresensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR:
                idx = R.string.channel_func_rainsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR:
                idx = R.string.channel_func_weightsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                idx = R.string.channel_func_noliquidsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                idx = R.string.channel_func_dimmer;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                idx = R.string.channel_func_rgblighting;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                idx = R.string.channel_func_dimmerandrgblighting;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
                idx = R.string.channel_func_depthsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
                idx = R.string.channel_func_distancesensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
                idx = R.string.channel_func_windowopeningsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:
                idx = R.string.channel_func_mailsensor;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
                idx = R.string.channel_func_staircasetimer;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
            case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
                idx = R.string.channel_func_electricitymeter;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
                idx = R.string.channel_func_gasmeter;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
                idx = R.string.channel_func_watermeter;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:
                idx = R.string.channel_func_heatmeter;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
                idx = R.string.channel_func_thermostat;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
                idx = R.string.channel_func_thermostat_hp_homeplus;
                break;
            case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
                idx = R.string.channel_func_valve;
                break;
        }

        return idx == -1 ? Integer.toString(func) : context.getResources().getString(idx);
    }

    public static int functionBit2functionNumber(int bit) {
        switch(bit) {
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
            case SUPLA_BIT_FUNC_POWERSWITCH:
                return SUPLA_CHANNELFNC_POWERSWITCH;
            case SUPLA_BIT_FUNC_LIGHTSWITCH:
                return SUPLA_CHANNELFNC_LIGHTSWITCH;
            case SUPLA_BIT_FUNC_STAIRCASETIMER:
                return SUPLA_CHANNELFNC_STAIRCASETIMER;
        }

        return 0;
    }
}
