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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.supla.android.Trace;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelGroup;
import org.supla.android.lib.SuplaChannelGroupRelation;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaChannelValueUpdate;
import org.supla.android.lib.SuplaLocation;

import java.util.ArrayList;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "supla.db";
    private Context context;
    private static final int DATABASE_VERSION = 6;
    private static final String M_DATABASE_NAME = "supla_measurements.db";
    private SQLiteDatabase rdb;

    public DbHelper(Context context, boolean measurements) {
        super(context, measurements ? M_DATABASE_NAME : DATABASE_NAME,
                null, DATABASE_VERSION);
        this.context = context;
        rdb = getReadableDatabase();
    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        rdb = getReadableDatabase();
    }

    private void execSQL(SQLiteDatabase db, String sql) {
        Trace.d("sql-statments", sql);
        db.execSQL(sql);
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

    private void createChannelTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE "
                + SuplaContract.ChannelEntry.TABLE_NAME + suffix + " (" +
                SuplaContract.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID + " INTEGER NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
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

    private void createChannelTable(SQLiteDatabase db) {
        createChannelTable(db, "");
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
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE + ", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION + " " +
                "FROM " + SuplaContract.ChannelEntry.TABLE_NAME + " C " +
                "JOIN " + SuplaContract.ChannelValueEntry.TABLE_NAME + " CV ON " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " = CV." +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID;


        execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);

    }

    private void createColorTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_COLOR_TABLE = "CREATE TABLE "
                + SuplaContract.ColorListItemEntry.TABLE_NAME + suffix + " (" +
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

    private void createColorTable(SQLiteDatabase db) {
        createColorTable(db, "");
    }


    private void createChannelGroupTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_CHANNELGROUP_TABLE = "CREATE TABLE "
                + SuplaContract.ChannelGroupEntry.TABLE_NAME + suffix + " (" +
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

    private void createChannelGroupTable(SQLiteDatabase db) {
        createChannelGroupTable(db, "");
    }

    private void createChannelGroupRelationTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_CHANNELGROUP_REL_TABLE = "CREATE TABLE " + SuplaContract.ChannelGroupRelationEntry.TABLE_NAME + suffix + " (" +
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

    private void createChannelGroupRelationTable(SQLiteDatabase db) {
        createChannelGroupRelationTable(db, "");
    }


    private void createChannelGroupValueView(SQLiteDatabase db, String suffix) {

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

    private void createChannelGroupValueView(SQLiteDatabase db) {
        createChannelGroupValueView(db, "");
    }

    private void createElectricityMeterLogTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_EMLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + suffix + " (" +
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
                " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_EMLOG_TABLE);
        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                +" )";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createElectricityMeterLogTable(SQLiteDatabase db) {
        createElectricityMeterLogTable(db, "");
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
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE
                + " FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + " WHERE "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED
                + " > 0";

        execSQL(db, SQL_CREATE_EM_VIEW);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLocationTable(db);
        createChannelTable(db);
        createChannelValueTable(db);
        createChannelView(db);
        createColorTable(db);
        createChannelGroupTable(db);
        createChannelGroupRelationTable(db);
        createChannelGroupValueView(db);
        createChannelExtendedValueTable(db);
        createElectricityMeterLogTable(db);
        createElectricityMeterLogView(db);
    }

    private void upgradeToV2(SQLiteDatabase db) {
        createColorTable(db);
    }

    private void upgradeToV3(SQLiteDatabase db) {
        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON
                + " INTEGER NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS
                + " INTEGER NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION
                + " INTEGER NOT NULL default 0");
    }

    private void upgradeToV4(SQLiteDatabase db) {
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
        createChannelView(db);
        createChannelGroupTable(db);
        createChannelGroupRelationTable(db);
        createChannelGroupValueView(db);

    }

    private void upgradeToV5(SQLiteDatabase db) {
        createChannelExtendedValueTable(db);
    }

    private void upgradeToV6(SQLiteDatabase db) {
        createElectricityMeterLogTable(db);
        createElectricityMeterLogView(db);
        execSQL(db, "DROP VIEW " + SuplaContract.ChannelViewEntry.VIEW_NAME);
        createChannelView(db);

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_DEVICEID
                + " INTEGER NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " INTEGER NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_MANUFACTURERID
                + " SMALLINT NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_PRODUCTID
                + " SMALLINT NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.ChannelGroupEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ChannelEntry.COLUMN_NAME_USERICON
                + " INTEGER NOT NULL default 0");

        execSQL(db, "ALTER TABLE " + SuplaContract.LocationEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                + " INTEGER NOT NULL default 0");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        execSQL(db, "DROP TABLE " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);
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
                }
            }
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
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_USERICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_MANUFACTURERID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PRODUCTID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION,

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

    public ChannelExtendedValue getChannelExtendedValue(int channelId) {

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

    public Cursor getChannelListCursor() {


        String sql = "SELECT "
                + "C." + SuplaContract.ChannelViewEntry._ID + " "
                + SuplaContract.ChannelViewEntry._ID
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_DEVICEID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE
                + ", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE + " "
                + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE
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

                + " FROM " + SuplaContract.ChannelViewEntry.VIEW_NAME + " C"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " = L."
                + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                + " WHERE C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + ", "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " DESC, "
                + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION;

        return rdb.rawQuery(sql, null);
    }

    public Cursor getGroupListCursor() {

        String sql = "SELECT "
                + "G." + SuplaContract.ChannelGroupEntry._ID + " "
                + SuplaContract.ChannelGroupEntry._ID
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                + ", L." + SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED
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

                + " FROM " + SuplaContract.ChannelGroupEntry.TABLE_NAME + " G"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID + " = L."
                + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                + " WHERE G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + ", "
                + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC + " DESC, "
                + "G." + SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION;


        return rdb.rawQuery(sql, null);
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


    public int getElectricityMeterMaxTimestamp(int channelId) {

        String selection = "SELECT MAX("
                +SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP+") FROM "
                +SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                +" WHERE "+SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID
                +" = "+Integer.toString(channelId);

        int max;

        SQLiteDatabase db = getReadableDatabase();

        //execSQL(db, "DELETE FROM "+SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);

        Cursor c = db.rawQuery(selection, null);
        c.moveToFirst();
        max = c.getInt(0);
        c.close();
        db.close();

        return max;
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

    public Cursor getElectricityMeasurements(SQLiteDatabase db, int channelId, String GroupByDateFormat) {

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
                + Integer.toString(channelId)
                +" GROUP BY "
                + " strftime('"
                + GroupByDateFormat
                + "', " + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ")"
                +" ORDER BY "
                +SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                + " ASC ";


        return db.rawQuery(sql, null);
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
}
