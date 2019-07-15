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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.supla.android.Trace;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaChannelValueUpdate;
import org.supla.android.lib.SuplaLocation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "supla.db";
    private Context context;
    private static final int DATABASE_VERSION = 12;
    private static final String M_DATABASE_NAME = "supla_measurements.db";
    private boolean measurements;

    public DbHelper(Context context, boolean measurements) {
        super(context, measurements ? M_DATABASE_NAME : DATABASE_NAME,
                null, DATABASE_VERSION);
        this.context = context;
        this.measurements = measurements;
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private void execSQL(SQLiteDatabase db, String sql) {
        Trace.d("sql-statments/"+(measurements ? M_DATABASE_NAME : DATABASE_NAME), sql);
        db.execSQL(sql);
    }

    private void addColumn(SQLiteDatabase db, String sql) {
        try {
            execSQL(db, sql);
        } catch(SQLException e) {
            if ( !e.getMessage().contains("duplicate column name:") ) {
                throw e;
            } else {
                e.getStackTrace();
            }
        }
    }

    private void createIndex(SQLiteDatabase db, String tableName, String fieldName) {
        final String SQL_CREATE_INDEX = "CREATE INDEX " + tableName + "_"
                + fieldName + "_index ON " + tableName + "(" + fieldName + ")";
        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createLocationTable(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE "
                + SuplaContract.LocationEntry.TABLE_NAME + " (" +
                SuplaContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED + " INTEGER NOT NULL default 0)";

        execSQL(db, SQL_CREATE_LOCATION_TABLE);
        createIndex(db, SuplaContract.LocationEntry.TABLE_NAME,
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID);
    }

    private void createChannelTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE "
                + SuplaContract.ChannelEntry.TABLE_NAME + " (" +
                SuplaContract.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID + " INTEGER NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_TYPE + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_FUNC + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID + " SMALLINT NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID + " SMALLINT NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_CHANNEL_TABLE);
        createIndex(db, SuplaContract.ChannelEntry.TABLE_NAME,
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID);
        createIndex(db, SuplaContract.ChannelEntry.TABLE_NAME,
                SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID);
    }

    private void createChannelValueTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELVALUE_TABLE = "CREATE TABLE "
                + SuplaContract.ChannelValueEntry.TABLE_NAME + " (" +
                SuplaContract.ChannelValueEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + " INTEGER NOT NULL," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE + " TEXT," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE + " TEXT)";


        execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);
        createIndex(db, SuplaContract.ChannelValueEntry.TABLE_NAME,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID);
    }

    private void createChannelExtendedValueTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELEXTENDEDVALUE_TABLE = "CREATE TABLE " +
                SuplaContract.ChannelExtendedValueEntry.TABLE_NAME + " (" +
                SuplaContract.ChannelExtendedValueEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_TYPE + " INTEGER NOT NULL," +
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE + " BLOB)";


        execSQL(db, SQL_CREATE_CHANNELEXTENDEDVALUE_TABLE);
        createIndex(db, SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID);
    }

    private void createChannelView(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELVALUE_TABLE = "CREATE VIEW "
                + SuplaContract.ChannelViewEntry.VIEW_NAME + " AS " +
                "SELECT C." + SuplaContract.ChannelEntry._ID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + ", " +
                "CV." + SuplaContract.ChannelValueEntry._ID + ", " +
                "CEV." + SuplaContract.ChannelExtendedValueEntry._ID + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE + ", " +
                "CEV." + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE + ", " +
                "CEV." + SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_TYPE + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_TYPE + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION + ", " +
                "I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1 + " " +
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1 + ", " +
                "I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2 + " " +
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2 + ", " +
                "I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3 + " " +
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3 + ", " +
                "I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4 + " " +
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4 + " " +
                "FROM " + SuplaContract.ChannelEntry.TABLE_NAME + " C " +
                "JOIN " + SuplaContract.ChannelValueEntry.TABLE_NAME + " CV ON " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " = CV." +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID + " " +
                "LEFT JOIN " + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME + " CEV ON " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " = CEV." +
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID + " " +
                "LEFT JOIN " + SuplaContract.UserIconsEntry.TABLE_NAME + " I ON " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + " = I." +
                SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID;

        execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);
    }

    private void createColorTable(SQLiteDatabase db) {

        final String SQL_CREATE_COLOR_TABLE = "CREATE TABLE "
                + SuplaContract.ColorListItemEntry.TABLE_NAME + " (" +
                SuplaContract.ColorListItemEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_COLOR_TABLE);
        createIndex(db, SuplaContract.ColorListItemEntry.TABLE_NAME,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID);
        createIndex(db, SuplaContract.ColorListItemEntry.TABLE_NAME,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP);
    }

    private void createChannelGroupTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELGROUP_TABLE = "CREATE TABLE "
                + SuplaContract.ChannelGroupEntry.TABLE_NAME + " (" +
                SuplaContract.ChannelGroupEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE + " TEXT)";

        execSQL(db, SQL_CREATE_CHANNELGROUP_TABLE);
        createIndex(db, SuplaContract.ChannelGroupEntry.TABLE_NAME,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID);
        createIndex(db, SuplaContract.ChannelGroupEntry.TABLE_NAME,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID);
    }

    private void createChannelGroupRelationTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELGROUP_REL_TABLE = "CREATE TABLE " + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME + " (" +
                SuplaContract.ChannelGroupRelationEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_CHANNELGROUP_REL_TABLE);
        createIndex(db, SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID);
        createIndex(db, SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID);
    }

    private void createChannelGroupValueView(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELGROUP_VALUE_VIEW =
                "CREATE VIEW " + SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME + " AS " +
                        "SELECT V." + SuplaContract.ChannelGroupValueViewEntry._ID + " "
                        + SuplaContract.ChannelGroupValueViewEntry._ID + ", "
                        + " G." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID + ", "
                        + "G." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC + ", "
                        + "R." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID + ", "
                        + "V." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE + ", "
                        + "V." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE + ", "
                        + "V." + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE + " "
                        + SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE + " "
                        + " FROM " + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME + " R "
                        + " JOIN " + SuplaContract.ChannelGroupEntry.TABLE_NAME + " G ON G."
                        + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID + " = R."
                        + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID
                        + " JOIN " + SuplaContract.ChannelValueEntry.TABLE_NAME + " V ON V."
                        + SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID + " = R."
                        + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID
                        + " WHERE R." + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE
                        + " > 0 AND "
                        + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " > 0";

        execSQL(db, SQL_CREATE_CHANNELGROUP_VALUE_VIEW);
    }

    private void createElectricityMeterLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_EMLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + " (" +
                SuplaContract.ElectricityMeterLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED +
                " INTEGER NOT NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT +
                " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_EMLOG_TABLE);
        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                +" )";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createElectricityMeterLogView(SQLiteDatabase db) {

        final String SQL_CREATE_EM_VIEW = "CREATE VIEW "
                + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME + " AS "
                + "SELECT " + SuplaContract.ElectricityMeterLogEntry._ID + " "
                + SuplaContract.ElectricityMeterLogViewEntry._ID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + ", "
                + "datetime("+SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP
                + ", 'unixepoch', 'localtime') "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE+", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE+" "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE+", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE+" "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE+", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE+" "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE+", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT+" "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_COMPLEMENT
                + " FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " > 0";

        execSQL(db, SQL_CREATE_EM_VIEW);
    }

    private void createThermostatLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_THLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ThermostatLogEntry.TABLE_NAME + " (" +
                SuplaContract.ThermostatLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_ON + " INTEGER NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE
                + " DECIMAL(5,2) NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE
                + " DECIMAL(5,2) NULL)";

        execSQL(db, SQL_CREATE_THLOG_TABLE);
        createIndex(db, SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME
                + "(" + SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createUserIconsTable(SQLiteDatabase db) {

        final String SQL_CREATE_IMAGE_TABLE = "CREATE TABLE " +
                SuplaContract.UserIconsEntry.TABLE_NAME + " (" +
                SuplaContract.UserIconsEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID + " INTEGER NOT NULL," +
                SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1 + " BLOB," +
                SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2 + " BLOB," +
                SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3 + " BLOB," +
                SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4 + " BLOB)";

        execSQL(db, SQL_CREATE_IMAGE_TABLE);
        createIndex(db, SuplaContract.UserIconsEntry.TABLE_NAME ,
                SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.UserIconsEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.UserIconsEntry.TABLE_NAME
                + "(" + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createImpulseCounterLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_ICLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + " (" +
                SuplaContract.ImpulseCounterLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER + " BIGINT NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE
                + " DOUBLE NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " INTEGER NOT NULL,"+
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT
                + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_ICLOG_TABLE);
        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP);

        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createImpulseCounterLogView(SQLiteDatabase db) {

        final String SQL_CREATE_EM_VIEW = "CREATE VIEW "
                + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME + " AS "
                + "SELECT " + SuplaContract.ImpulseCounterLogEntry._ID + " "
                + SuplaContract.ImpulseCounterLogViewEntry._ID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP + ", "
                + "datetime("+SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP
                + ", 'unixepoch', 'localtime') "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE+", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER+" "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER+", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE+" "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE+", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT+" "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COMPLEMENT
                + " FROM " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " > 0";

        execSQL(db, SQL_CREATE_EM_VIEW);
    }

    private void createTemperatureLogTable(SQLiteDatabase db) {
        final String SQL_CREATE_TLOG_TABLE = "CREATE TABLE " +
                SuplaContract.TemperatureLogEntry.TABLE_NAME + " (" +
                SuplaContract.TemperatureLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE
                + " DECIMAL(8,4) NULL)";

        execSQL(db, SQL_CREATE_TLOG_TABLE);
        createIndex(db, SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME
                + "(" + SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createTempHumidityLogTable(SQLiteDatabase db) {
        final String SQL_CREATE_THLOG_TABLE = "CREATE TABLE " +
                SuplaContract.TempHumidityLogEntry.TABLE_NAME + " (" +
                SuplaContract.TempHumidityLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TEMPERATURE
                + " DECIMAL(8,4) NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_HUMIDITY
                + " DECIMAL(8,4) NULL)";

        execSQL(db, SQL_CREATE_THLOG_TABLE);
        createIndex(db, SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME
                + "(" + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLocationTable(db);
        createChannelTable(db);
        createChannelValueTable(db);
        createColorTable(db);
        createChannelGroupTable(db);
        createChannelGroupRelationTable(db);
        createChannelExtendedValueTable(db);
        createElectricityMeterLogTable(db);
        createThermostatLogTable(db);
        createUserIconsTable(db);
        createImpulseCounterLogTable(db);
        createTemperatureLogTable(db);
        createTempHumidityLogTable(db);

        // Create views at the end
        createChannelView(db);
        createChannelGroupValueView(db);
        createElectricityMeterLogView(db);
        createImpulseCounterLogView(db);
    }

    private void upgradeToV2(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV2");
        createColorTable(db);
    }

    private void upgradeToV3(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV3");
        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION
                + " INTEGER NOT NULL default 0");
    }

    private void upgradeToV4(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV4");
        execSQL(db, "ALTER TABLE " + SuplaContract.ColorListItemEntry.TABLE_NAME +
                " RENAME TO " + SuplaContract.ColorListItemEntry.TABLE_NAME + "_old");

        createColorTable(db);

        final String SQL_COPY = "INSERT INTO " + SuplaContract.ColorListItemEntry.TABLE_NAME + " (" +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID + "," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP + "," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + "," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR + "," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS + ") " +
                "SELECT c.channelid, 0, ci.idx, ci.color, ci.brightness FROM channel c JOIN color_list_item_old ci ON c._id = ci.channel";

        execSQL(db, SQL_COPY);

        execSQL(db, "DROP TABLE " + SuplaContract.ColorListItemEntry.TABLE_NAME + "_old");

        execSQL(db, "DROP TABLE accessid");
        execSQL(db, "DROP TABLE " + SuplaContract.LocationEntry.TABLE_NAME);
        execSQL(db, "DROP TABLE " + SuplaContract.ChannelEntry.TABLE_NAME);

        createLocationTable(db);
        createChannelTable(db);
        createChannelValueTable(db);
        createChannelGroupTable(db);
        createChannelGroupRelationTable(db);
    }

    private void upgradeToV5(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV5");
        createChannelExtendedValueTable(db);
    }

    private void upgradeToV6(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV6");

        createElectricityMeterLogTable(db);

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID
                + " SMALLINT NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_TYPE
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID
                + " SMALLINT NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.ChannelGroupEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " INTEGER NOT NULL default 0");

        addColumn(db, "ALTER TABLE " + SuplaContract.LocationEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                + " INTEGER NOT NULL default 0");

    }

    private void upgradeToV7(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV7");
        createThermostatLogTable(db);
    }

    private void upgradeToV8(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV8");
        createUserIconsTable(db);
    }

    private void upgradeToV9(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV9");
        execSQL(db, "DROP TABLE " + SuplaContract.ChannelValueEntry.TABLE_NAME);
        execSQL(db, "DROP TABLE " + SuplaContract.ChannelExtendedValueEntry.TABLE_NAME);
        createChannelValueTable(db);
        createChannelExtendedValueTable(db);


        createImpulseCounterLogTable(db);
        createTemperatureLogTable(db);
        createTempHumidityLogTable(db);
    }

    private void upgradeToV10(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV10");
        execSQL(db, "DROP TABLE " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
        createImpulseCounterLogTable(db);
    }

    private void upgradeToV11(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV11");

        execSQL(db, "DROP TABLE " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);
        execSQL(db, "DROP TABLE " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);

        createElectricityMeterLogTable(db);
        createImpulseCounterLogTable(db);
    }

    private void upgradeToV12(SQLiteDatabase db) {
        execSQL(db, "DELETE FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);
        execSQL(db, "DELETE FROM " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
    }


    private void recreateViews(SQLiteDatabase db) {
        execSQL(db, "DROP VIEW IF EXISTS "
                + SuplaContract.ChannelViewEntry.VIEW_NAME);
        execSQL(db, "DROP VIEW IF EXISTS "
                + SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME);
        execSQL(db, "DROP VIEW IF EXISTS "
                + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME);
        execSQL(db, "DROP VIEW IF EXISTS "
                + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME);

        createChannelView(db);
        createChannelGroupValueView(db);
        createElectricityMeterLogView(db);
        createImpulseCounterLogView(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        execSQL(db, "DROP TABLE " + SuplaContract.UserIconsEntry.TABLE_NAME);
        execSQL(db, "DROP VIEW " + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME);
        */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < newVersion) {

            for (int nv = oldVersion; nv < newVersion; nv++) {
                switch (nv) {
                    case 1:
                        upgradeToV2(db);
                        break;
                    case 2:
                        upgradeToV3(db);
                        break;
                    case 3:
                        upgradeToV4(db);
                        break;
                    case 4:
                        upgradeToV5(db);
                        break;
                    case 5:
                        upgradeToV6(db);
                        break;
                    case 6:
                        upgradeToV7(db);
                        break;
                    case 7:
                        upgradeToV8(db);
                        break;
                    case 8:
                        upgradeToV9(db);
                        break;
                    case 9:
                        upgradeToV10(db);
                        break;
                    case 10:
                        upgradeToV11(db);
                        break;
                    case 11:
                        upgradeToV12(db);
                        break;
                }
            }

            // Recreate views on the end
            recreateViews(db);
        }

    }

    public Location getLocation(int locationId) {

        Location result = null;
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.LocationEntry._ID,
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION,
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
        };

        String selection = SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " = ?";

        String[] selectionArgs = {
                String.valueOf(locationId),
        };

        Cursor c = db.query(
                SuplaContract.LocationEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );


        if (c.getCount() > 0) {

            c.moveToFirst();

            result = new Location();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }

    public boolean updateLocation(SuplaLocation suplaLocation) {

        Location location = getLocation(suplaLocation.Id);
        SQLiteDatabase db = null;

        if (location == null) {

            location = new Location();
            location.AssignSuplaLocation(suplaLocation);
            location.setVisible(1);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.LocationEntry.TABLE_NAME,
                    null,
                    location.getContentValues());

        } else if (location.Diff(suplaLocation)) {

            db = getWritableDatabase();

            location.AssignSuplaLocation(suplaLocation);
            location.setVisible(1);

            String selection = SuplaContract.LocationEntry._ID + " LIKE ?";
            String[] selectionArgs = {String.valueOf(location.getId())};

            db.update(
                    SuplaContract.LocationEntry.TABLE_NAME,
                    location.getContentValues(),
                    selection,
                    selectionArgs);

        }

        if (db != null) {
            db.close();
            return true;
        }


        return false;
    }

    public boolean updateLocation(Location location) {

        SQLiteDatabase db = null;

        if (location != null) {

            db = getWritableDatabase();

            String selection = SuplaContract.LocationEntry._ID + " LIKE ?";
            String[] selectionArgs = {String.valueOf(location.getId())};

            db.update(
                    SuplaContract.LocationEntry.TABLE_NAME,
                    location.getContentValues(),
                    selection,
                    selectionArgs);

        }

        if (db != null) {
            db.close();
            return true;
        }


        return false;
    }

    private DbItem getItem(String ClassName, String[] projection, String tableName,
                           String id1Field, int id1, String id2Field, int id2) {

        DbItem result = null;
        SQLiteDatabase db = getReadableDatabase();

        String selection = id1Field + " = ?";

        if (id2 != 0) {
            selection += " AND " + id2Field + " = ?";
        }

        String[] selectionArgs1 = {
                String.valueOf(id1),
        };

        String[] selectionArgs2 = {
                String.valueOf(id1),
                String.valueOf(id2),
        };

        Cursor c = db.query(
                tableName,
                projection,
                selection,
                id2 == 0 ? selectionArgs1 : selectionArgs2,
                null,
                null,
                null
        );


        if (c.getCount() > 0) {

            c.moveToFirst();

            try {
                result = (DbItem) Class.forName(ClassName).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (result != null) {
                result.AssignCursorData(c);
            }

        }

        c.close();
        db.close();

        return result;

    }

    private DbItem getItem(String ClassName, String[] projection, String tableName,
                           String id1Field, int id1) {
        return getItem(ClassName, projection, tableName, id1Field, id1, "", 0);
    }

    public Channel getChannel(int channelId) {

        String[] projection = {
                SuplaContract.ChannelViewEntry._ID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUETYPE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4,
        };

        return (Channel) getItem("org.supla.android.db.Channel",
                projection,
                SuplaContract.ChannelViewEntry.VIEW_NAME,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID,
                channelId);

    }


    private ChannelValue getChannelValue(int channelId) {

        String[] projection = {
                SuplaContract.ChannelValueEntry._ID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE,
        };

        return (ChannelValue) getItem("org.supla.android.db.ChannelValue",
                projection,
                SuplaContract.ChannelValueEntry.TABLE_NAME,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID,
                channelId);

    }

    public ChannelGroup getChannelGroup(int groupId) {

        String[] projection = {
                SuplaContract.ChannelGroupEntry._ID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE,
        };

        return (ChannelGroup) getItem("org.supla.android.db.ChannelGroup",
                projection,
                SuplaContract.ChannelGroupEntry.TABLE_NAME,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID,
                groupId);

    }

    private ChannelGroupRelation getChannelGroupRelation(int groupId, int channelId) {

        String[] projection = {
                SuplaContract.ChannelGroupRelationEntry._ID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE,
        };

        return (ChannelGroupRelation) getItem("org.supla.android.db.ChannelGroupRelation",
                projection,
                SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID,
                groupId,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID,
                channelId);

    }

    private void updateDbItem(SQLiteDatabase db, DbItem item, String idField, String tableName,
                              long id) {

        String selection = idField + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(
                tableName,
                item.getContentValues(),
                selection,
                selectionArgs);
    }

    public boolean updateChannel(SuplaChannel suplaChannel) {


        Location location = getLocation(suplaChannel.LocationID);
        SQLiteDatabase db = null;

        if (location != null) {

            Channel channel = getChannel(suplaChannel.Id);

            if (channel == null) {

                channel = new Channel();
                channel.Assign(suplaChannel);
                channel.setVisible(1);

                db = getWritableDatabase();
                db.insert(
                        SuplaContract.ChannelEntry.TABLE_NAME,
                        null,
                        channel.getContentValues());

            } else if (channel.Diff(suplaChannel)
                    || channel.getLocationId() != suplaChannel.LocationID
                    || channel.getVisible() != 1) {

                db = getWritableDatabase();

                channel.Assign(suplaChannel);
                channel.setVisible(1);

                updateDbItem(db, channel, SuplaContract.ChannelEntry._ID,
                        SuplaContract.ChannelEntry.TABLE_NAME, channel.getId());
            }


            if (db != null) {
                db.close();
                return true;
            }

        }


        return false;
    }

    public boolean updateChannelValue(SuplaChannelValue suplaChannelValue, int channelId,
                                      boolean onLine) {

        SQLiteDatabase db = null;
        ChannelValue value = getChannelValue(channelId);

        if (value == null) {

            value = new ChannelValue();
            value.AssignSuplaChannelValue(suplaChannelValue);
            value.setChannelId(channelId);
            value.setOnLine(onLine);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ChannelValueEntry.TABLE_NAME,
                    null,
                    value.getContentValues());

        } else if (value.Diff(suplaChannelValue)
                || value.getOnLine() != onLine) {

            db = getWritableDatabase();

            value.AssignSuplaChannelValue(suplaChannelValue);
            value.setOnLine(onLine);


            updateDbItem(db, value, SuplaContract.ChannelValueEntry._ID,
                    SuplaContract.ChannelValueEntry.TABLE_NAME, value.getId());
        }

        if (db != null) {
            db.close();
            return true;
        }

        return false;
    }

    public boolean updateChannelValue(SuplaChannelValueUpdate channelValue) {
        return updateChannelValue(channelValue.Value, channelValue.Id, channelValue.OnLine);
    }

    private ChannelExtendedValue getChannelExtendedValue(int channelId) {

        String[] projection = {
                SuplaContract.ChannelExtendedValueEntry._ID,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_TYPE,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE,
        };

        return (ChannelExtendedValue) getItem("org.supla.android.db.ChannelExtendedValue",
                projection,
                SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_CHANNELID,
                channelId);

    }

    public boolean updateChannelExtendedValue(SuplaChannelExtendedValue suplaChannelExtendedValue,
                                              int channelId) {

        SQLiteDatabase db;
        ChannelExtendedValue value = getChannelExtendedValue(channelId);


        if (value == null) {
            value = new ChannelExtendedValue();
            value.setExtendedValue(suplaChannelExtendedValue);
            value.setChannelId(channelId);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ChannelExtendedValueEntry.TABLE_NAME,
                    null,
                    value.getContentValues());
        } else {
            db = getWritableDatabase();
            value.setExtendedValue(suplaChannelExtendedValue);
            updateDbItem(db, value, SuplaContract.ChannelExtendedValueEntry._ID,
                    SuplaContract.ChannelExtendedValueEntry.TABLE_NAME, value.getId());
        }

        db.close();
        return true;

    }

    private void updateChannelGroup(SQLiteDatabase db, ChannelGroup channelGroup) {

        SQLiteDatabase _db = db;
        if (_db == null) {
            _db = getWritableDatabase();
        }

        updateDbItem(_db, channelGroup, SuplaContract.ChannelGroupEntry._ID,
                SuplaContract.ChannelGroupEntry.TABLE_NAME, channelGroup.getId());

        if (db == null) {
            _db.close();
        }
    }

    private void updateChannelGroup(ChannelGroup channelGroup) {
        updateChannelGroup(null, channelGroup);
    }

    public boolean updateChannelGroup(SuplaChannelGroup suplaChannelGroup) {

        Location location = getLocation(suplaChannelGroup.LocationID);
        SQLiteDatabase db = null;

        if (location != null) {

            ChannelGroup cgroup = getChannelGroup(suplaChannelGroup.Id);

            if (cgroup == null) {

                cgroup = new ChannelGroup();
                cgroup.Assign(suplaChannelGroup);
                cgroup.setVisible(1);

                db = getWritableDatabase();
                db.insert(
                        SuplaContract.ChannelGroupEntry.TABLE_NAME,
                        null,
                        cgroup.getContentValues());

            } else if (cgroup.Diff(suplaChannelGroup)
                    || cgroup.getLocationId() != suplaChannelGroup.LocationID
                    || cgroup.getVisible() != 1) {

                db = getWritableDatabase();

                cgroup.Assign(suplaChannelGroup);
                cgroup.setVisible(1);

                updateChannelGroup(db, cgroup);
            }

            if (db != null) {
                db.close();
                return true;
            }

        }

        return false;
    }

    public boolean updateChannelGroupRelation(SuplaChannelGroupRelation suplaChannelGroupRelation) {

        SQLiteDatabase db = null;

        ChannelGroupRelation cgrel = getChannelGroupRelation(suplaChannelGroupRelation.ChannelGroupID,
                suplaChannelGroupRelation.ChannelID);

        if (cgrel == null) {

            cgrel = new ChannelGroupRelation();
            cgrel.Assign(suplaChannelGroupRelation);
            cgrel.setVisible(1);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                    null,
                    cgrel.getContentValues());

        } else if (cgrel.getVisible() != 1) {

            db = getWritableDatabase();

            cgrel.Assign(suplaChannelGroupRelation);
            cgrel.setVisible(1);

            updateDbItem(db, cgrel, SuplaContract.ChannelGroupRelationEntry._ID,
                    SuplaContract.ChannelGroupRelationEntry.TABLE_NAME, cgrel.getId());
        }

        if (db != null) {
            db.close();
            return true;
        }

        return false;
    }

    private boolean setVisible(String table, String field, int Visible, int WhereVisible) {

        String selection = field + " = ?";
        String[] selectionArgs = {String.valueOf(WhereVisible)};

        ContentValues values = new ContentValues();
        values.put(field, Visible);

        SQLiteDatabase db = getWritableDatabase();

        int count = db.update(
                table,
                values,
                selection,
                selectionArgs);

        db.close();

        return count > 0;

    }

    public boolean setChannelsVisible(int Visible, int WhereVisible) {
        return setVisible(SuplaContract.ChannelEntry.TABLE_NAME,
                SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, Visible, WhereVisible);
    }

    public boolean setChannelGroupsVisible(int Visible, int WhereVisible) {

        return setVisible(SuplaContract.ChannelGroupEntry.TABLE_NAME,
                SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE, Visible, WhereVisible);
    }

    public boolean setChannelGroupRelationsVisible(int Visible, int WhereVisible) {

        return setVisible(SuplaContract.ChannelGroupRelationEntry.TABLE_NAME,
                SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE, Visible, WhereVisible);
    }

    public boolean setChannelsOffline() {

        String selection = SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + " = ?";
        String[] selectionArgs = {String.valueOf(1)};

        ContentValues values = new ContentValues();
        values.put(SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE, 0);

        SQLiteDatabase db = getWritableDatabase();

        int count = db.update(
                SuplaContract.ChannelValueEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();

        return count > 0;
    }

    public int getChannelCount() {

        String selection = "SELECT count(*) FROM " + SuplaContract.ChannelEntry.TABLE_NAME;

        int count;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selection, null);
        c.moveToFirst();
        count = c.getInt(0);
        c.close();
        db.close();

        return count;

    }

    public Cursor getChannelListCursor(String WHERE) {

        SQLiteDatabase db = getReadableDatabase();

        if (WHERE != null && WHERE.length() > 0) {
            WHERE = " AND ("+WHERE+")";
        } else {
            WHERE = "";
        }

        String sql = "SELECT "
                + "C." + SuplaContract.ChannelViewEntry._ID + " "
                + SuplaContract.ChannelViewEntry._ID
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED + " "
                + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_TYPE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUEID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUEID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUETYPE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_EXTENDEDVALUETYPE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1 + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE1
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2 + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE2
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3 + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE3
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4 + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON_IMAGE4

                + " FROM " + SuplaContract.ChannelViewEntry.VIEW_NAME + " C"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " = L."
                + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                + " WHERE C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " > 0 "
                + WHERE
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + ", "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " DESC, "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION;

        return db.rawQuery(sql, null);
    }

    public Cursor getChannelListCursor() {
        return getChannelListCursor("");
    }

    public Cursor getChannelListCursorForGroup(int groupId) {

        String WHERE = "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
                + " IN ( SELECT "+SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID
                + " FROM " + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME
                + " WHERE " + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID
                + " = " + Integer.toString(groupId)
                + " AND " + SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE
                + " > 0 ) ";

        return getChannelListCursor(WHERE);
    }

    public Cursor getGroupListCursor() {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT "
                + "G." + SuplaContract.ChannelGroupEntry._ID + " "
                + SuplaContract.ChannelGroupEntry._ID
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED + " "
                + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_TOTALVALUE
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS + " "
                + ", G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " "
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE
                + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1 + " "
                + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
                + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2 + " "
                + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
                + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3 + " "
                + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
                + ", I." + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4 + " "
                + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4

                + " FROM " + SuplaContract.ChannelGroupEntry.TABLE_NAME + " G"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " = L."
                + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                + " LEFT JOIN " + SuplaContract.UserIconsEntry.TABLE_NAME + " I"
                + " ON G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " = I."
                + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " WHERE G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + ", "
                + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " DESC, "
                + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION;


        return db.rawQuery(sql, null);
    }

    public ColorListItem getColorListItem(int id, boolean group, int idx) {

        ColorListItem result = null;
        SQLiteDatabase db = getReadableDatabase();


        String[] projection = {
                SuplaContract.ColorListItemEntry._ID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS,

        };

        String selection = "( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID + " = ?" +
                " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP + " = ?" +
                " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ? )";

        String[] selectionArgs = {
                String.valueOf(id),
                String.valueOf(group ? 1 : 0),
                String.valueOf(idx),
        };


        Cursor c = db.query(
                SuplaContract.ColorListItemEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() > 0) {

            c.moveToFirst();

            result = new ColorListItem();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }

    public void updateColorListItemValue(ColorListItem item) {

        ColorListItem cli = getColorListItem(item.getRemoteId(), item.getGroup(), item.getIdx());
        SQLiteDatabase db = getWritableDatabase();

        if (cli == null) {

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    null,
                    item.getContentValues());

        } else {

            cli.AssignColorListItem(item);

            String selection = SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID + " = ? "+
                    " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP + " = ? " +
                    " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ?";

            String[] selectionArgs = {
                    String.valueOf(cli.getRemoteId()),
                    String.valueOf(cli.getGroup() ? 1 : 0),
                    String.valueOf(cli.getIdx())
            };

            db.update(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    cli.getContentValues(),
                    selection,
                    selectionArgs);

        }


        db.close();
    }

    public Integer[] updateChannelGroups() {

        ArrayList<Integer> result = new ArrayList<Integer>();

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.ChannelGroupValueViewEntry._ID,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_VALUE,
        };

        Cursor c = db.query(
                SuplaContract.ChannelGroupValueViewEntry.VIEW_NAME,
                projection,
                null,
                null,
                null,
                null,
                SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID
        );

        ChannelGroup cgroup = null;

        if (c.moveToFirst())
            do {

                int GroupId = c.getInt(c.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID));

                if (cgroup == null) {
                    cgroup = getChannelGroup(GroupId);
                    if (cgroup == null) {
                        break;
                    }

                    cgroup.resetBuffer();
                }

                if (cgroup.getGroupId() == GroupId) {
                    ChannelValue val = new ChannelValue();
                    val.AssignCursorDataFromGroupView(c);
                    cgroup.addValueToBuffer(val);
                }

                if (!c.isLast()) {
                    c.moveToNext();
                    GroupId = c.getInt(c.getColumnIndex(SuplaContract.ChannelGroupValueViewEntry.COLUMN_NAME_GROUPID));
                    c.moveToPrevious();
                }

                if (c.isLast() || cgroup.getGroupId() != GroupId) {
                    if (cgroup.DiffWithBuffer()) {
                        cgroup.assignBuffer();
                        updateChannelGroup(cgroup);
                        result.add(cgroup.getGroupId());
                    }

                    if (!c.isLast()) {
                        cgroup = null;
                    }
                }

            } while (c.moveToNext());

        c.close();
        db.close();

        return result.toArray(new Integer[0]);
    }

    private Calendar lastSecondInMonthWithOffset(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.add(Calendar.SECOND, -1);
        return calendar;
    }

    private double getLastMeasurementValue(String tableName, String colTimestamp,
                                           String colChannelId, String colValue, int monthOffset,
                                           int channelId) {
        double result = 0;

        String[] projection = {
                "SUM("+colValue+")"
        };

        String selection = colChannelId
                + " = ? AND " + colTimestamp + " <= ?";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(lastSecondInMonthWithOffset(monthOffset).getTimeInMillis()/1000)
        };

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                colTimestamp +" DESC",
                "1");

        result = c.getCount();

        if (c.getCount() > 0) {
            c.moveToFirst();
            result = c.getDouble(0);
        }

        c.close();
        db.close();

        return result;
    }

    public double getLastImpulseCounterMeasurementValue(int monthOffset,
                                           int channelId) {
        return getLastMeasurementValue(SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE,
                 monthOffset, channelId);
    }

    public double getLastElectricityMeterMeasurementValue(int monthOffset,
                                                        int channelId) {
        return getLastMeasurementValue(SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME,
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID,
                "IFNULL("
                        +SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE
                        + ", 0) + IFNULL("
                        + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE
                        + ",0) + IFNULL("
                        + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE
                        +",0)",
                monthOffset, channelId);
    }

    private int getMeasurementTimestamp(String tableName, String colTimestamp,
                                        String colChannelId, int channelId, boolean min) {

        String selection = "SELECT "+
                (min ? "MIN" : "MAX")
                +"("
                +colTimestamp+") FROM "
                +tableName
                +" WHERE "+colChannelId
                +" = "+Integer.toString(channelId);

        int max;

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(selection, null);
        c.moveToFirst();
        max = c.getInt(0);
        c.close();
        db.close();

        return max;
    }

    private int getTotalCount(String tableName, String colChannelId, int channelId,
                              String andWhere) {

        String selection = "SELECT COUNT(*) FROM "
                +tableName
                +" WHERE "+colChannelId
                +" = "+Integer.toString(channelId)
                +andWhere;

        int total = 0;

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(selection, null);
        c.moveToFirst();
        total = c.getInt(0);
        c.close();
        db.close();

        return total;
    }

    private int getTotalCount(String tableName, String colChannelId, int channelId) {
        return getTotalCount(tableName, colChannelId, channelId, "");
    }

    private boolean timestampStartsWithTheCurrentMonth(long TS) {
        if (TS == 0) {
            return true;
        } else {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());

            Calendar minDate = Calendar.getInstance();
            minDate.setTime(new Date(TS*1000));

            return minDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && minDate.get(Calendar.MONTH) == now.get(Calendar.MONTH);
        }

    }

    public int getElectricityMeterMeasurementTimestamp(int channelId, boolean min) {

        return getMeasurementTimestamp(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public boolean electricityMeterMeasurementsStartsWithTheCurrentMonth(int channelId) {
        long minTS = getElectricityMeterMeasurementTimestamp(channelId,
                true);
        return timestampStartsWithTheCurrentMonth(minTS);
    }

    public int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement) {

        String complementCondition = "";
        if (withoutComplement) {
            complementCondition = " AND " +
                    SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT + " = 0";
        }

        return getTotalCount(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID,
                channelId,
                complementCondition);
    }

    public ElectricityMeasurementItem getOlderUncalculatedElectricityMeasurement(
            SQLiteDatabase db, int channelId, long timestamp) {
        String[] projection = {
                SuplaContract.ElectricityMeterLogEntry._ID,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT
        };

        String selection = SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID
                + " = ? AND " + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP
                + " < ? AND " + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " = 0";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(timestamp)
        };

        Cursor c = db.query(
                SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP +" DESC",
                "1"
        );

        ElectricityMeasurementItem emi = null;
        if (c.getCount() > 0) {
            emi = new ElectricityMeasurementItem();
            c.moveToFirst();
            emi.AssignCursorData(c);
        }

        c.close();

        return emi;
    }

    public void addElectricityMeasurement(SQLiteDatabase db,
                                          ElectricityMeasurementItem emi) {
        db.insertWithOnConflict(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                null, emi.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getElectricityMeasurements(SQLiteDatabase db, int channelId,
                                             String GroupByDateFormat,
                                             Date dateFrom, Date dateTo) {

        String sql = "SELECT SUM("+SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE+")"+
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE + ", "
                + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE + ")" +
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE + ", "
                + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE + ")" +
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE + ", "
                + " MAX(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ")" +
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ", "
                + " MAX(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + ")" +
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME
                + " WHERE "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID
                + " = "
                + Integer.toString(channelId);

        if (dateFrom != null && dateTo != null) {
            sql += " AND "
                    + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " >= " + Long.toString(dateFrom.getTime() / 1000)
                    + " AND "
                    + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " <= " + Long.toString(dateTo.getTime() / 1000);
        }

        sql += " GROUP BY "
                + " strftime('"
                + GroupByDateFormat
                + "', " + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ")"
                +" ORDER BY "
                +SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";

        return db.rawQuery(sql, null);
    }

    public void deleteElectricityMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public void deleteUncalculatedElectricityMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                        + " = 0 AND "
                        + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public int getThermostatMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getThermostatMeasurementTotalCount(int channelId) {
        return getTotalCount(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, channelId);
    }

    public void deleteThermostatMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public void addThermostatMeasurement(SQLiteDatabase db,
                                          ThermostatMeasurementItem emi) {
        db.insertWithOnConflict(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                null, emi.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getThermostatMeasurements(SQLiteDatabase db, int channelId, String GroupByDateFormat) {

        String sql = "SELECT "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.ThermostatLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID
                + " = "
                + Integer.toString(channelId)
                +" ORDER BY "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";


        return db.rawQuery(sql, null);
    }

    public int getTempHumidityMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getTempHumidityMeasurementTotalCount(int channelId) {
        return getTotalCount(SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID, channelId);
    }

    public void deleteTempHumidityMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public void addTempHumidityMeasurement(SQLiteDatabase db,
                                           TempHumidityMeasurementItem emi) {
        db.insertWithOnConflict(SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                null, emi.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getTempHumidityMeasurements(SQLiteDatabase db, int channelId,
                                              String GroupByDateFormat,
                                              Date dateFrom, Date dateTo) {

        String sql = "SELECT "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TEMPERATURE + ", "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_HUMIDITY + ", "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.TempHumidityLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID
                + " = "
                + Integer.toString(channelId);

        if (dateFrom != null && dateTo != null) {
            sql += " AND "
                    + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP
                    + " >= " + Long.toString(dateFrom.getTime() / 1000)
                    + " AND "
                    + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP
                    + " <= " + Long.toString(dateTo.getTime() / 1000);
        }

        sql += " ORDER BY "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";
        
        return db.rawQuery(sql, null);
    }

    public Cursor getTempHumidityMeasurements(SQLiteDatabase db, int channelId,
                                              String GroupByDateFormat) {
        return getTempHumidityMeasurements(db, channelId, GroupByDateFormat,
                null, null);
    }

    public int getTemperatureMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getTemperatureMeasurementTotalCount(int channelId) {
        return getTotalCount(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, channelId);
    }

    public void deleteTemperatureMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public void addTemperatureMeasurement(SQLiteDatabase db,
                                          TemperatureMeasurementItem emi) {
        db.insertWithOnConflict(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                null, emi.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getTemperatureMeasurements(SQLiteDatabase db, int channelId,
                                             String GroupByDateFormat, Date dateFrom, Date dateTo) {

        String sql = "SELECT "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE + ", "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.TemperatureLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID
                + " = "
                + Integer.toString(channelId);

        if (dateFrom != null && dateTo != null) {
            sql += " AND "
                    + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP
                    + " >= " + Long.toString(dateFrom.getTime() / 1000)
                    + " AND "
                    + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP
                    + " <= " + Long.toString(dateTo.getTime() / 1000);
        }

        sql += " ORDER BY "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";


        return db.rawQuery(sql, null);
    }

    public Cursor getTemperatureMeasurements(SQLiteDatabase db, int channelId,
                                             String GroupByDateFormat) {
        return getTemperatureMeasurements(db, channelId, GroupByDateFormat,
                null, null);
    }

    public void addImpulseCounterMeasurement(SQLiteDatabase db,
                                          ImpulseCounterMeasurementItem item) {
        db.insertWithOnConflict(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                null, item.getContentValues(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int getImpulseCounterMeasurementTimestamp(int channelId, boolean min) {

        return getMeasurementTimestamp(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public boolean impulseCounterMeasurementsStartsWithTheCurrentMonth(int channelId) {
        long minTS = getImpulseCounterMeasurementTimestamp(channelId,
                true);
        return timestampStartsWithTheCurrentMonth(minTS);
    }

    public int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement) {

        String complementCondition = "";

        if (withoutComplement) {
            complementCondition = " AND " +
                    SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT + " = 0";
        }

        return getTotalCount(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId,
                complementCondition);
    }

    public ImpulseCounterMeasurementItem getOlderUncalculatedImpulseCounterMeasurement(
            SQLiteDatabase db, int channelId, long timestamp) {
        String[] projection = {
                SuplaContract.ImpulseCounterLogEntry._ID,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT
        };

        String selection = SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID
                + " = ? AND " + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP
                + " < ? AND " + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " = 0";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(timestamp)
        };

        Cursor c = db.query(
                SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP +" DESC",
                "1"
        );

        ImpulseCounterMeasurementItem item = null;
        if (c.getCount() > 0) {
            item = new ImpulseCounterMeasurementItem();
            c.moveToFirst();
            item.AssignCursorData(c);
        }

        c.close();

        return item;
    }

    public Cursor getImpulseCounterMeasurements(SQLiteDatabase db, int channelId,
                                                String GroupByDateFormat,
                                                Date dateFrom, Date dateTo) {

        String sql = "SELECT SUM("
                +SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER+")"+
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER + ", "
                + " SUM("
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE + ")"
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE + ", "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME
                + " WHERE "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID
                + " = "
                + Integer.toString(channelId);

        if (dateFrom != null && dateTo != null) {
            sql += " AND "
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " >= " + Long.toString(dateFrom.getTime() / 1000)
                    + " AND "
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " <= " + Long.toString(dateTo.getTime() / 1000);
        }

        sql += " GROUP BY "
                + " strftime('"
                + GroupByDateFormat
                + "', " + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE + ")"
                +" ORDER BY "
                +SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";


        return db.rawQuery(sql, null);
    }

    public void deleteImpulseCounterMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public void deleteUncalculatedImpulseCounterMeasurements(SQLiteDatabase db, int channelId) {
        String[] args = {
                String.valueOf(channelId),
        };

        db.delete(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                        + " = 0 AND "
                        + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID
                        + " = ?",
                args);
    }

    public ArrayList<Integer> iconsToDownload(SQLiteDatabase db) {
        ArrayList<Integer> ids = new ArrayList<Integer>();

        String sql = "SELECT C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " FROM " +SuplaContract.ChannelEntry.TABLE_NAME + " AS C"
                + " LEFT JOIN "+SuplaContract.UserIconsEntry.TABLE_NAME + " AS U ON C."
                + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + " = "
                + "U."+SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " WHERE "+SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE +
                " > 0 AND "+SuplaContract.ChannelEntry.COLUMN_NAME_USERICON +
                " > 0 AND U."+SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " IS NULL";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(
                        cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON));
                if ( !ids.contains(id) ) {
                    ids.add(id);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        sql = "SELECT C." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                + " " + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON
                + " FROM " +SuplaContract.ChannelGroupEntry.TABLE_NAME + " AS C"
                + " LEFT JOIN "+SuplaContract.UserIconsEntry.TABLE_NAME + " AS U ON C."
                + SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON + " = "
                + "U."+SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " WHERE "+SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE +
                " > 0 AND "+SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON +
                " > 0 AND U."+SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + " IS NULL";

        cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(
                        cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_USERICON));
                if ( !ids.contains(id) ) {
                    ids.add(id);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();


        return ids;
    }

    public boolean addUserIcons(SQLiteDatabase db,
                             int Id, byte[] img1, byte[] img2, byte[] img3, byte[] img4) {

        if (db == null
                || Id <= 0
                || (img1 == null && img2 == null && img3 == null && img4 == null)) {
            return false;
        }

        ContentValues values = new ContentValues();

        values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID, Id);

        if (img1!= null) {
            values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1, img1);
            ImageCache.addImage(new ImageId(Id, 1), img1);
        }

        if (img2!= null) {
            values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2, img2);
            ImageCache.addImage(new ImageId(Id, 2), img2);
        }

        if (img3!= null) {
            values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3, img3);
            ImageCache.addImage(new ImageId(Id, 3), img3);
        }

        if (img4!= null) {
            values.put(SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4, img4);
            ImageCache.addImage(new ImageId(Id, 4), img4);
        }

        db.insertWithOnConflict(SuplaContract.UserIconsEntry.TABLE_NAME,
                null, values, SQLiteDatabase.CONFLICT_IGNORE);

        return true;
    }

    public void deleteUserIcons() {
        SQLiteDatabase db = getReadableDatabase();
        db.delete(SuplaContract.UserIconsEntry.TABLE_NAME, null, null);
        db.close();

        ImageCache.clear();
    }

    public void loadUserIconsIntoCache() {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT " + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3
                + ", " + SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4
                + " FROM " + SuplaContract.UserIconsEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                for (int a = 1; a <= 4; a++) {
                    String field = "";
                    switch (a) {
                        case 1:
                            field = SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE1;
                            break;
                        case 2:
                            field = SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE2;
                            break;
                        case 3:
                            field = SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE3;
                            break;
                        case 4:
                            field = SuplaContract.UserIconsEntry.COLUMN_NAME_IMAGE4;
                            break;
                    }

                    byte[] image = cursor.getBlob(cursor.getColumnIndex(field));
                    int remoteId = cursor.getInt(cursor.getColumnIndex(
                            SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID));

                    if (image != null && image.length > 0) {
                        ImageCache.addImage(new ImageId(remoteId, a), image);
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

    }
}
