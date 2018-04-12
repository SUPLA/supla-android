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
import org.supla.android.lib.Preferences;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaChannelValueUpdate;
import org.supla.android.lib.SuplaLocation;


public class DbHelper extends SQLiteOpenHelper {

    private SQLiteDatabase rdb = null;
    private Context context;
    private static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "supla.db";

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
        final String SQL_CREATE_INDEX = "CREATE INDEX "+tableName+"_" +fieldName +"_index ON "+tableName+"("+fieldName+")";
        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createLocationTable(SQLiteDatabase db) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + SuplaContract.LocationEntry.TABLE_NAME + " ("+
                SuplaContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_LOCATION_TABLE);
        createIndex(db, SuplaContract.LocationEntry.TABLE_NAME, SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID);
    }

    private void createChannelTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + SuplaContract.ChannelEntry.TABLE_NAME + suffix + " ("+
                SuplaContract.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_FUNC + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_CHANNEL_TABLE);
        createIndex(db, SuplaContract.ChannelEntry.TABLE_NAME, SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID);
        createIndex(db, SuplaContract.ChannelEntry.TABLE_NAME, SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID);
    }

    private void createChannelTable(SQLiteDatabase db) {
        createChannelTable(db, "");
    }

    private void createChannelValueTable(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELVALUE_TABLE = "CREATE TABLE " + SuplaContract.ChannelValueEntry.TABLE_NAME + " ("+
                SuplaContract.ChannelValueEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + " INTEGER NOT NULL," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE + " TEXT," +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE + " TEXT)";


        execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);
        createIndex(db, SuplaContract.ChannelValueEntry.TABLE_NAME, SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID);
    }

    private void createChannelView(SQLiteDatabase db) {

        final String SQL_CREATE_CHANNELVALUE_TABLE = "CREATE VIEW " + SuplaContract.ChannelViewEntry.VIEW_NAME + " AS " +
                "SELECT C."+SuplaContract.ChannelEntry._ID + ", " +
                "C."+SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + ", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION +", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE +", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE +", " +
                "CV." + SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS +", " +
                "C." + SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION + " " +
                "FROM " + SuplaContract.ChannelEntry.TABLE_NAME + " C " +
                "JOIN " + SuplaContract.ChannelValueEntry.TABLE_NAME + " CV ON " +
                "C."+ SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " = CV." +
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID;


        execSQL(db, SQL_CREATE_CHANNELVALUE_TABLE);

    }

    private void createColorTable(SQLiteDatabase db, String suffix) {

        final String SQL_CREATE_COLOR_TABLE = "CREATE TABLE " + SuplaContract.ColorListItemEntry.TABLE_NAME + suffix + " ("+
                SuplaContract.ColorListItemEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_COLOR_TABLE);
        createIndex(db, SuplaContract.ColorListItemEntry.TABLE_NAME, SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID);
    }

    private void createColorTable(SQLiteDatabase db) {
        createColorTable(db, "");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLocationTable(db);
        createChannelTable(db);
        createChannelValueTable(db);
        createChannelView(db);
        createColorTable(db);
    }

    private void upgradeToV2(SQLiteDatabase db) {
        createColorTable(db);
    }

    private void upgradeToV3(SQLiteDatabase db) {
        execSQL(db, "ALTER TABLE "+SuplaContract.ChannelEntry.TABLE_NAME+" ADD COLUMN "+SuplaContract.ChannelEntry.COLUMN_NAME_ALTICON+" INTEGER NOT NULL default 0");
        execSQL(db, "ALTER TABLE "+SuplaContract.ChannelEntry.TABLE_NAME+" ADD COLUMN "+SuplaContract.ChannelEntry.COLUMN_NAME_FLAGS+" INTEGER NOT NULL default 0");
        execSQL(db, "ALTER TABLE "+SuplaContract.ChannelEntry.TABLE_NAME+" ADD COLUMN "+SuplaContract.ChannelEntry.COLUMN_NAME_PROTOCOLVERSION+" INTEGER NOT NULL default 0");
    }

    private void upgradeToV4(SQLiteDatabase db) {

        execSQL(db, "ALTER TABLE "+SuplaContract.ColorListItemEntry.TABLE_NAME+" RENAME TO "+SuplaContract.ColorListItemEntry.TABLE_NAME+"_old");
        createColorTable(db);

        final String SQL_COPY = "INSERT INTO "+SuplaContract.ColorListItemEntry.TABLE_NAME+" ("+
                SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID+","+
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX+","+
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR+","+
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS+") "+
                "SELECT c.channelid, ci.idx, ci.color, ci.brightness FROM channel c JOIN color_list_item_old ci ON c._id = ci.channel";

        execSQL(db, SQL_COPY);

        execSQL(db, "DROP TABLE "+SuplaContract.ColorListItemEntry.TABLE_NAME+"_old");

        execSQL(db, "DROP TABLE accessid");
        execSQL(db, "DROP TABLE "+SuplaContract.LocationEntry.TABLE_NAME);
        execSQL(db, "DROP TABLE "+SuplaContract.ChannelEntry.TABLE_NAME);

        createLocationTable(db);
        createChannelTable(db);
        createChannelValueTable(db);
        createChannelView(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if ( oldVersion < newVersion ) {

            for(int nv=oldVersion;nv<newVersion;nv++) {

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
                }
            }
        }

    }

    public Location getLocation(long id, boolean primary_key) {

        Location result = null;
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.LocationEntry._ID,
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION,
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE
        };

        String selection = (primary_key ? SuplaContract.LocationEntry._ID : SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID ) + " = ?";

        String[] selectionArgs = {
                String.valueOf(id),
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


        if ( c.getCount() > 0 ) {

            c.moveToFirst();

            result = new Location();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }


    public boolean updateLocation(SuplaLocation suplaLocation) {

        Location location = getLocation(suplaLocation.Id, false);
        SQLiteDatabase db = null;

        if ( location == null ) {

            location = new Location();
            location.AssignSuplaLocation(suplaLocation);
            location.setVisible(1);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.LocationEntry.TABLE_NAME,
                    null,
                    location.getContentValues());

        } else if ( location.Diff(suplaLocation) ) {

            db = getWritableDatabase();

            location.AssignSuplaLocation(suplaLocation);
            location.setVisible(1);

            String selection = SuplaContract.LocationEntry._ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(location.getId()) };

            db.update(
                    SuplaContract.LocationEntry.TABLE_NAME,
                    location.getContentValues(),
                    selection,
                    selectionArgs);

        }

        if ( db != null ) {
            db.close();
            return true;
        }


        return false;
    }

    public Channel getChannel(long id, boolean primary_key) {

        Channel result = null;
        SQLiteDatabase db = getReadableDatabase();


        String[] projection = {
                SuplaContract.ChannelViewEntry._ID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS,
                SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION,

        };

        String selection = (primary_key ? SuplaContract.ChannelViewEntry._ID : SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID ) + " = ?";

        String[] selectionArgs = {
                String.valueOf(id),
        };

        Cursor c = db.query(
                SuplaContract.ChannelViewEntry.VIEW_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );


        if ( c.getCount() > 0 ) {

            c.moveToFirst();

            result = new Channel();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }

    public ChannelValue getChannelValue(int channelId) {

        ChannelValue result = null;
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.ChannelValueEntry._ID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelValueEntry.COLUMN_NAME_VALUE,
        };

        String selection = SuplaContract.ChannelValueEntry.COLUMN_NAME_CHANNELID + " = ?";

        String[] selectionArgs = {
                String.valueOf(channelId),
        };


        Cursor c = db.query(
                SuplaContract.ChannelValueEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if ( c.getCount() > 0 ) {
            c.moveToFirst();

            result = new ChannelValue();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }

    private void _updateChannel(SQLiteDatabase db, Channel channel) {

        String selection = SuplaContract.ChannelEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(channel.getId()) };

        db.update(
                SuplaContract.ChannelEntry.TABLE_NAME,
                channel.getContentValues(),
                selection,
                selectionArgs);
    }

    private void _updateChannelValue(SQLiteDatabase db, ChannelValue cvalue) {

        String selection = SuplaContract.ChannelValueEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(cvalue.getId()) };

        db.update(
                SuplaContract.ChannelValueEntry.TABLE_NAME,
                cvalue.getContentValues(),
                selection,
                selectionArgs);
    }

    public boolean updateChannel(SuplaChannel suplaChannel) {


            Location location = getLocation(suplaChannel.LocationID, false);
            SQLiteDatabase db = null;

            if ( location != null ) {

                Channel channel = getChannel(suplaChannel.Id, false);

                if ( channel == null ) {

                    channel = new Channel();
                    channel.AssignSuplaChannel(suplaChannel);
                    channel.setVisible(1);
                    channel.setLocationId(suplaChannel.LocationID);
                    channel.setAltIcon(suplaChannel.AltIcon);
                    channel.setFlags(suplaChannel.Flags);
                    channel.setProtocolVersion(suplaChannel.ProtocolVersion);

                    db = getWritableDatabase();
                    db.insert(
                            SuplaContract.ChannelEntry.TABLE_NAME,
                            null,
                            channel.getContentValues());

                } else if ( channel.Diff(suplaChannel)
                            || channel.getLocationId() != suplaChannel.LocationID
                            || channel.getVisible() != 1 ) {

                    db = getWritableDatabase();

                    channel.AssignSuplaChannel(suplaChannel);
                    channel.setLocationId(suplaChannel.LocationID);
                    channel.setVisible(1);
                    channel.setAltIcon(suplaChannel.AltIcon);
                    channel.setFlags(suplaChannel.Flags);
                    channel.setProtocolVersion(suplaChannel.ProtocolVersion);

                    _updateChannel(db, channel);
                }


                if ( db != null ) {
                    db.close();
                    return true;
                }

            }


        return false;
    }

    public boolean updateChannelValue(SuplaChannelValue suplaChannelValue, int channelId, boolean onLine) {

        SQLiteDatabase db = null;
        ChannelValue value = getChannelValue(channelId);

        if ( value == null ) {

            value = new ChannelValue();
            value.AssignSuplaChannelValue(suplaChannelValue);
            value.setChannelId(channelId);
            value.setOnLine(onLine);

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ChannelValueEntry.TABLE_NAME,
                    null,
                    value.getContentValues());

        } else if ( value.Diff(suplaChannelValue)
                || value.getOnLine() != onLine ) {

            db = getWritableDatabase();

            value.AssignSuplaChannelValue(suplaChannelValue);
            value.setOnLine(onLine);

            _updateChannelValue(db, value);
        }

        if ( db != null ) {
            db.close();
            return true;
        }

        return false;
    }

    public boolean updateChannelValue(SuplaChannelValueUpdate channelValue) {
        return updateChannelValue(channelValue.Value, channelValue.Id, channelValue.OnLine);
    }

    public boolean setChannelsVisible(int Visible, int WhereVisible) {

        String selection = SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + " = ?";
        String[] selectionArgs = { String.valueOf(WhereVisible) };

        ContentValues values = new ContentValues();
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE, Visible);

        SQLiteDatabase db = getWritableDatabase();

        int count = db.update(
                SuplaContract.ChannelEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();

        return count > 0;
    }

    public boolean setChannelsOffline() {

        String selection = SuplaContract.ChannelValueEntry.COLUMN_NAME_ONLINE + " = ?";
        String[] selectionArgs = { String.valueOf(1) };

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

        int count = 0;

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
                +"C."+ SuplaContract.ChannelViewEntry._ID + " "
                     + SuplaContract.ChannelViewEntry._ID
                +", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_CHANNELID
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_ONLINE
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_SUBVALUE
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_VALUE
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_ALTICON
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_FLAGS
                +", C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION + " "
                        + SuplaContract.ChannelViewEntry.COLUMN_NAME_PROTOCOLVERSION

                + " FROM " + SuplaContract.ChannelViewEntry.VIEW_NAME + " C"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_LOCATIONID + " = L."
                           + SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID
                + " WHERE C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION+", "
                               + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_FUNC+" DESC, "
                               + "C." + SuplaContract.ChannelViewEntry.COLUMN_NAME_CAPTION;

        return rdb.rawQuery(sql, null);
    }


    public ColorListItem getColorListItem(int channelId, int idx) {

        ColorListItem result = null;
        SQLiteDatabase db = getReadableDatabase();


        String[] projection = {
                SuplaContract.ColorListItemEntry._ID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS,

        };

        String selection = "( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID + " = ?" +
                " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ? )";

        String[] selectionArgs = {
                String.valueOf(channelId),
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

        if ( c.getCount() > 0 ) {

            c.moveToFirst();

            result = new ColorListItem();
            result.AssignCursorData(c);
        }

        c.close();
        db.close();

        return result;
    }

    public void updateColorListItemValue(ColorListItem item) {

        ColorListItem cli = getColorListItem(item.getChannelId(), item.getIdx());

        SQLiteDatabase db = getWritableDatabase();

        if ( cli == null ) {

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    null,
                    item.getContentValues());

        } else {

            cli.AssignColorListItem(item);

            String selection = "( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNELID + " = ? ) AND ( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ? )";
            String[] selectionArgs = { String.valueOf(cli.getChannelId()), String.valueOf(cli.getIdx()) };

            db.update(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    cli.getContentValues(),
                    selection,
                    selectionArgs);

        }


        db.close();
    }

}
