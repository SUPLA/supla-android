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

  public abstract static class ElectricityMeterLogEntry implements BaseColumns {

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

    public static final String COLUMN_NAME_FAE_BALANCED = "fae_balanced";
    public static final String COLUMN_NAME_RAE_BALANCED = "rae_balanced";

    public static final String COLUMN_NAME_COMPLEMENT = "complement";
    public static final String COLUMN_NAME_PROFILEID = "profileid";
  }

  public abstract static class ElectricityMeterLogViewEntry implements BaseColumns {

    public static final String VIEW_NAME = "em_log_v1";

    public static final String _ID = "_id"; // Primary Key
    public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
    public static final String COLUMN_NAME_TIMESTAMP = "date";
    public static final String COLUMN_NAME_DATE = "date_dt";

    public static final String COLUMN_NAME_PHASE1_FAE = "phase1_fae";
    public static final String COLUMN_NAME_PHASE2_FAE = "phase2_fae";
    public static final String COLUMN_NAME_PHASE3_FAE = "phase3_fae";
    public static final String COLUMN_NAME_PHASE1_RAE = "phase1_rae";
    public static final String COLUMN_NAME_PHASE2_RAE = "phase2_rae";
    public static final String COLUMN_NAME_PHASE3_RAE = "phase3_rae";

    public static final String COLUMN_NAME_FAE_BALANCED = "fae_balanced";
    public static final String COLUMN_NAME_RAE_BALANCED = "rae_balanced";

    public static final String COLUMN_NAME_COMPLEMENT = "complement";
    public static final String COLUMN_NAME_PROFILEID = "profileid";
  }

  public abstract static class ImpulseCounterLogEntry implements BaseColumns {

    public static final String TABLE_NAME = "ic_log";

    public static final String _ID = "_ic_id"; // Primary Key
    public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
    public static final String COLUMN_NAME_TIMESTAMP = "date";
    public static final String COLUMN_NAME_COUNTER = "counter";
    public static final String COLUMN_NAME_CALCULATEDVALUE = "calculated_value";
    public static final String COLUMN_NAME_COMPLEMENT = "complement";
    public static final String COLUMN_NAME_PROFILEID = "profileid";
  }

  public abstract static class ImpulseCounterLogViewEntry implements BaseColumns {

    public static final String VIEW_NAME = "ic_log_v1";

    public static final String _ID = "_ic_id"; // Primary Key
    public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
    public static final String COLUMN_NAME_TIMESTAMP = "date";
    public static final String COLUMN_NAME_DATE = "date_dt";

    public static final String COLUMN_NAME_COUNTER = "counter";
    public static final String COLUMN_NAME_CALCULATEDVALUE = "calculated_value";
    public static final String COLUMN_NAME_COMPLEMENT = "complement";
    public static final String COLUMN_NAME_PROFILEID = "profileid";
  }

  public abstract static class ThermostatLogEntry implements BaseColumns {

    public static final String TABLE_NAME = "th_log";

    public static final String _ID = "_id"; // Primary Key
    public static final String COLUMN_NAME_CHANNELID = "channelid"; // SuplaChannel.Id
    public static final String COLUMN_NAME_TIMESTAMP = "date";

    public static final String COLUMN_NAME_ON = "ison";
    public static final String COLUMN_NAME_MEASUREDTEMPERATURE = "measured";
    public static final String COLUMN_NAME_PRESETTEMPERATURE = "preset";
    public static final String COLUMN_NAME_PROFILEID = "profileid";
  }
}
