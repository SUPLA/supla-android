package org.supla.android.lib;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

public class SuplaConst {

    public final static int SUPLA_GUID_SIZE = 16;
    public final static int SUPLA_CLIENT_NAME_MAXSIZE = 201;
    public final static int SUPLA_ACCESSID_PWD_MAXSIZE = 33;
    public final static int SUPLA_SOFTVER_MAXSIZE = 21;
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


    public final static int SUPLA_CHANNELFNC_NONE                          =  0;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK     = 10;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGATE            = 20;
    public final static int SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR      = 30;
    public final static int SUPLA_CHANNELFNC_THERMOMETER                   = 40;
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


    public final static int SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK          = 10;
    public final static int SUPLA_EVENT_CONTROLLINGTHEGATE                 = 20;
    public final static int SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR           = 30;
    public final static int SUPLA_EVENT_CONTROLLINGTHEDOORLOCK             = 40;
    public final static int SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER        = 50;
    public final static int SUPLA_EVENT_POWERONOFF                         = 60;
    public final static int SUPLA_EVENT_LIGHTONOFF                         = 70;
}
