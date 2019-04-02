package org.supla.android.db;

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

import android.provider.BaseColumns;

public class SuplaContract {

    public SuplaContract() {}


    public static abstract class LocationEntry implements BaseColumns {

        public static final String TABLE_NAME = "location";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_LOCATIONID = "locationid"; // SuplaLocation.Id
        public static final String COLUMN_NAME_CAPTION = "caption";
        public static final String COLUMN_NAME_VISIBLE = "visible";
        public static final String COLUMN_NAME_COLLAPSED = "collapsed";
    }

    public static abstract class ChannelEntry implements BaseColumns {

        public static final String TABLE_NAME = "channel";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_DEVICEID = "deviceid";
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_CAPTION = "caption";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_FUNC = "func";
        public static final String COLUMN_NAME_VISIBLE = "visible";
        public static final String COLUMN_NAME_LOCATIONID = "locatonid"; // SuplaLocation.Id
        public static final String COLUMN_NAME_ALTICON = "alticon";
        public static final String COLUMN_NAME_USERICON = "usericon";
        public static final String COLUMN_NAME_MANUFACTURERID = "manufacturerid";
        public static final String COLUMN_NAME_PRODUCTID = "productid";
        public static final String COLUMN_NAME_FLAGS = "flags";
        public static final String COLUMN_NAME_PROTOCOLVERSION = "protocolversion";

    }

    public static abstract class ChannelValueEntry implements BaseColumns {

        public static final String TABLE_NAME = "channel_value";

        public static final String _ID = "_channel_value_id"; // Primary Key
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_ONLINE = "online";
        public static final String COLUMN_NAME_SUBVALUE = "subvalue";
        public static final String COLUMN_NAME_VALUE = "value";
    }

    public static abstract class ChannelExtendedValueEntry implements BaseColumns {

        public static final String TABLE_NAME = "channel_extendedvalue";

        public static final String _ID = "_channel_extendedvalue_id"; // Primary Key
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_TYPE = "extendedvaluetype";
        public static final String COLUMN_NAME_VALUE = "extendedvalue";
    }

    public static abstract class ChannelViewEntry implements BaseColumns {

        public static final String VIEW_NAME = "channel_v1";

        public static final String _ID = ChannelEntry._ID;
        public static final String COLUMN_NAME_DEVICEID = ChannelEntry.COLUMN_NAME_DEVICEID;
        public static final String COLUMN_NAME_CHANNELID = ChannelEntry.COLUMN_NAME_CHANNELID;
        public static final String COLUMN_NAME_CAPTION = ChannelEntry.COLUMN_NAME_CAPTION;
        public static final String COLUMN_NAME_VALUEID = ChannelValueEntry._ID;
        public static final String COLUMN_NAME_EXTENDEDVALUEID = ChannelExtendedValueEntry._ID;
        public static final String COLUMN_NAME_ONLINE = ChannelValueEntry.COLUMN_NAME_ONLINE;
        public static final String COLUMN_NAME_SUBVALUE = ChannelValueEntry.COLUMN_NAME_SUBVALUE;
        public static final String COLUMN_NAME_VALUE = ChannelValueEntry.COLUMN_NAME_VALUE;
        public static final String COLUMN_NAME_EXTENDEDVALUETYPE =
                ChannelExtendedValueEntry.COLUMN_NAME_TYPE;
        public static final String COLUMN_NAME_EXTENDEDVALUE =
                ChannelExtendedValueEntry.COLUMN_NAME_VALUE;
        public static final String COLUMN_NAME_TYPE = ChannelEntry.COLUMN_NAME_TYPE;
        public static final String COLUMN_NAME_FUNC = ChannelEntry.COLUMN_NAME_FUNC;
        public static final String COLUMN_NAME_VISIBLE = ChannelEntry.COLUMN_NAME_VISIBLE;
        public static final String COLUMN_NAME_LOCATIONID = ChannelEntry.COLUMN_NAME_LOCATIONID;
        public static final String COLUMN_NAME_ALTICON = ChannelEntry.COLUMN_NAME_ALTICON;
        public static final String COLUMN_NAME_USERICON = ChannelEntry.COLUMN_NAME_USERICON;
        public static final String COLUMN_NAME_MANUFACTURERID = ChannelEntry.COLUMN_NAME_MANUFACTURERID;
        public static final String COLUMN_NAME_PRODUCTID = ChannelEntry.COLUMN_NAME_PRODUCTID;
        public static final String COLUMN_NAME_FLAGS = ChannelEntry.COLUMN_NAME_FLAGS;
        public static final String COLUMN_NAME_PROTOCOLVERSION = ChannelEntry.COLUMN_NAME_PROTOCOLVERSION;
        public static final String COLUMN_NAME_USERICON_IMAGE1 = UserIconsEntry.COLUMN_NAME_IMAGE1;
        public static final String COLUMN_NAME_USERICON_IMAGE2 = UserIconsEntry.COLUMN_NAME_IMAGE2;
        public static final String COLUMN_NAME_USERICON_IMAGE3 = UserIconsEntry.COLUMN_NAME_IMAGE3;
        public static final String COLUMN_NAME_USERICON_IMAGE4 = UserIconsEntry.COLUMN_NAME_IMAGE4;
    }

    public static abstract class ColorListItemEntry implements BaseColumns {

        public static final String TABLE_NAME = "color_list_item";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_REMOTEID = "remoteid"; // SuplaChannel.Id or SuplaChannelGroup.Id
        public static final String COLUMN_NAME_GROUP = "isgroup";
        public static final String COLUMN_NAME_IDX = "idx";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_BRIGHTNESS = "brightness";
    }


    public static abstract class ChannelGroupEntry implements BaseColumns {

        public static final String TABLE_NAME = "channelgroup";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_GROUPID = "groupid"; // SuplaChannelGroup.Id
        public static final String COLUMN_NAME_CAPTION = "caption";
        public static final String COLUMN_NAME_ONLINE = ChannelValueEntry.COLUMN_NAME_ONLINE;
        public static final String COLUMN_NAME_FUNC = "func";
        public static final String COLUMN_NAME_VISIBLE = "visible";
        public static final String COLUMN_NAME_LOCATIONID = "locatonid"; // SuplaLocation.Id
        public static final String COLUMN_NAME_ALTICON = "alticon";
        public static final String COLUMN_NAME_USERICON = "usericon";
        public static final String COLUMN_NAME_FLAGS = "flags";
        public static final String COLUMN_NAME_TOTALVALUE = "totalvalue";
    }

    public static abstract class ChannelGroupRelationEntry implements BaseColumns {

        public static final String TABLE_NAME = "channelgroup_rel";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_GROUPID = "groupid"; // SuplaChannelGroup.Id
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_VISIBLE = "visible";

    }


    public static abstract class ChannelGroupValueViewEntry implements BaseColumns {

        public static final String VIEW_NAME = "channelgroupvalue_v1";

        public static final String _ID = ChannelValueEntry._ID;
        public static final String COLUMN_NAME_GROUPID = ChannelGroupEntry.COLUMN_NAME_GROUPID;
        public static final String COLUMN_NAME_FUNC = ChannelGroupEntry.COLUMN_NAME_FUNC;
        public static final String COLUMN_NAME_CHANNELID = ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID;
        public static final String COLUMN_NAME_ONLINE = ChannelValueEntry.COLUMN_NAME_ONLINE;
        public static final String COLUMN_NAME_SUBVALUE = ChannelValueEntry.COLUMN_NAME_SUBVALUE;
        public static final String COLUMN_NAME_VALUE = ChannelValueEntry.COLUMN_NAME_VALUE;

    }


    public static abstract class ElectricityMeterLogEntry implements BaseColumns {

        public static final String TABLE_NAME = "em_log";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_TIMESTAMP = "date";

        public static final String COLUMN_NAME_PHASE1_FAE = "phase1_fae";
        public static final String COLUMN_NAME_PHASE1_RAE = "phase1_rae";
        public static final String COLUMN_NAME_PHASE1_FRE = "phase1_fre";
        public static final String COLUMN_NAME_PHASE1_RRE = "phase1_rre";

        public static final String COLUMN_NAME_PHASE2_FAE = "phase2_fae";
        public static final String COLUMN_NAME_PHASE2_RAE = "phase2_rae";
        public static final String COLUMN_NAME_PHASE2_FRE = "phase2_fre";
        public static final String COLUMN_NAME_PHASE2_RRE = "phase2_rre";

        public static final String COLUMN_NAME_PHASE3_FAE = "phase3_fae";
        public static final String COLUMN_NAME_PHASE3_RAE = "phase3_rae";
        public static final String COLUMN_NAME_PHASE3_FRE = "phase3_fre";
        public static final String COLUMN_NAME_PHASE3_RRE = "phase3_rre";

        public static final String COLUMN_NAME_INCREASE_CALCULATED = "inc_calculated";
    }

    public static abstract class ElectricityMeterLogViewEntry implements BaseColumns {

        public static final String VIEW_NAME = "em_log_v1";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_TIMESTAMP = "date";
        public static final String COLUMN_NAME_DATE = "date_dt";

        public static final String COLUMN_NAME_PHASE1_FAE = "phase1_fae";
        public static final String COLUMN_NAME_PHASE2_FAE = "phase2_fae";
        public static final String COLUMN_NAME_PHASE3_FAE = "phase3_fae";

    }

    public static abstract class ThermostatLogEntry implements BaseColumns {

        public static final String TABLE_NAME = "th_log";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
        public static final String COLUMN_NAME_TIMESTAMP = "date";

        public static final String COLUMN_NAME_ON = "ison";
        public static final String COLUMN_NAME_MEASUREDTEMPERATURE = "measured";
        public static final String COLUMN_NAME_PRESETTEMPERATURE = "preset";
    }

    public static abstract class UserIconsEntry implements BaseColumns {

        public static final String TABLE_NAME = "user_icons";

        public static final String _ID = "_id"; // Primary Key
        public static final String COLUMN_NAME_REMOTEID = "remoteid";
        public static final String COLUMN_NAME_IMAGE1 = "uimage1";
        public static final String COLUMN_NAME_IMAGE2 = "image2";
        public static final String COLUMN_NAME_IMAGE3 = "image3";
        public static final String COLUMN_NAME_IMAGE4 = "image4";
    }
}
