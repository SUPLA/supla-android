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

public class SuplaConst {

    public final static int SUPLA_GUID_SIZE = 16;
    public final static int SUPLA_AUTHKEY_SIZE = 16;
    public final static int SUPLA_CLIENT_NAME_MAXSIZE = 201;
    public final static int SUPLA_ACCESSID_PWD_MAXSIZE = 33;
    public final static int SUPLA_SOFTVER_MAXSIZE = 21;
    public final static int SUPLA_EMAIL_MAXSIZE = 256;
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
    public final static int EM_VAR_ALL = 0xFFFF;

    public final static int EV_TYPE_ELECTRICITY_METER_MEASUREMENT_V1 = 10;

}
