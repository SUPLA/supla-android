package org.supla.android.db;

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.supla.android.Trace;
import org.supla.android.lib.Preferences;
import org.supla.android.lib.SuplaChannel;
import org.supla.android.lib.SuplaChannelValueUpdate;
import org.supla.android.lib.SuplaLocation;


public class DbHelper extends SQLiteOpenHelper {

    private SQLiteDatabase rdb = null;
    private Context context;
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "supla.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        rdb = getReadableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_ACCESSID_TABLE = "CREATE TABLE " + SuplaContract.AccessIDEntry.TABLE_NAME + " ("+
                SuplaContract.AccessIDEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.AccessIDEntry.COLUMN_NAME_ACCESSID + " INTEGER NOT NULL," +
                SuplaContract.AccessIDEntry.COLUMN_NAME_SERVERADDRESS + " TEXT NOT NULL)";



        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + SuplaContract.LocationEntry.TABLE_NAME + " ("+
                SuplaContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID + ") REFERENCES " +
                SuplaContract.AccessIDEntry.TABLE_NAME + " (" + SuplaContract.AccessIDEntry._ID + "))";



        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + SuplaContract.ChannelEntry.TABLE_NAME + " ("+
                SuplaContract.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION + " TEXT NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_FUNC + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_SUBVALUE + " TEXT," +
                SuplaContract.ChannelEntry.COLUMN_NAME_VALUE + " TEXT," +
                SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + " INTEGER NOT NULL," +
                SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + ") REFERENCES " +
                SuplaContract.LocationEntry.TABLE_NAME + " (" + SuplaContract.LocationEntry._ID + "))";



        Trace.d("sql-statments", SQL_CREATE_ACCESSID_TABLE);
        Trace.d("sql-statments", SQL_CREATE_LOCATION_TABLE);
        Trace.d("sql-statments", SQL_CREATE_CHANNEL_TABLE);

        db.execSQL(SQL_CREATE_ACCESSID_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_CHANNEL_TABLE);

        upgradeToV2(db);

    }

    private void upgradeToV2(SQLiteDatabase db) {

        final String SQL_CREATE_COLOR_TABLE = "CREATE TABLE " + SuplaContract.ColorListItemEntry.TABLE_NAME + " ("+
                SuplaContract.ColorListItemEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNEL + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR + " INTEGER NOT NULL," +
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNEL + ") REFERENCES " +
                SuplaContract.ChannelEntry.TABLE_NAME + " (" + SuplaContract.ChannelEntry._ID + "))";

        Trace.d("sql-statments", SQL_CREATE_COLOR_TABLE);
        db.execSQL(SQL_CREATE_COLOR_TABLE);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if ( oldVersion == 2 && newVersion == 1 ) {

            final String SQL_DROP_COLOR_TABLE = "DROP TABLE IF EXISTS "+SuplaContract.ColorListItemEntry.TABLE_NAME;
            Trace.d("sql-statments", SQL_DROP_COLOR_TABLE);
            db.execSQL(SQL_DROP_COLOR_TABLE);

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if ( oldVersion == 1 && newVersion == 2 ) {
            upgradeToV2(db);
        }

    }

    public long getCurrentAccessId() {

        long result = 0;


        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.AccessIDEntry._ID,
        };

        String selection = SuplaContract.AccessIDEntry.COLUMN_NAME_ACCESSID + " = ? AND " +
                           SuplaContract.AccessIDEntry.COLUMN_NAME_SERVERADDRESS + " = ?";


        int AccessID = 0;
        String ServerAddress = "";

        {
            Preferences prefs = new Preferences(context);
            AccessID = prefs.getAccessID();
            ServerAddress = prefs.getServerAddress();
        }

        String[] selectionArgs = {
                String.valueOf(AccessID),
                ServerAddress,
        };

        Cursor c = db.query(
                SuplaContract.AccessIDEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if ( c.getCount() > 0 ) {

            c.moveToFirst();
            result = c.getInt(
                    c.getColumnIndex(SuplaContract.AccessIDEntry._ID)
            );

        }

        c.close();
        db.close();

        if ( result == 0 ) {
            db = getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(SuplaContract.AccessIDEntry.COLUMN_NAME_ACCESSID, AccessID);
            values.put(SuplaContract.AccessIDEntry.COLUMN_NAME_SERVERADDRESS, ServerAddress);

            result = db.insert(
                    SuplaContract.AccessIDEntry.TABLE_NAME,
                    null,
                    values);

            db.close();
        }

        return result;
    }

    public Location getLocation(long accessid, long id, boolean primary_key) {

        Location result = null;
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                SuplaContract.LocationEntry._ID,
                SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID,
                SuplaContract.LocationEntry.COLUMN_NAME_CAPTION,
                SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID,
        };

        String selection = SuplaContract.AccessIDEntry.COLUMN_NAME_ACCESSID + " = ? AND " +
                ( primary_key == true ? SuplaContract.LocationEntry._ID : SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID ) + " = ?";

        String[] selectionArgs = {
                String.valueOf(accessid),
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

        long accessid = getCurrentAccessId();

        if ( accessid != 0 ) {

            Location location = getLocation(accessid, suplaLocation.Id, false);
            SQLiteDatabase db = null;

            if ( location == null ) {

                location = new Location();
                location.setAccessId(accessid);
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
        }

        return false;
    }

    public Channel getChannel(long accessid, long id, boolean primary_key) {

        Channel result = null;
        SQLiteDatabase db = getReadableDatabase();


        String[] projection = {
                SuplaContract.ChannelEntry._ID,
                SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION,
                SuplaContract.ChannelEntry.COLUMN_NAME_FUNC,
                SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE,
                SuplaContract.ChannelEntry.COLUMN_NAME_SUBVALUE,
                SuplaContract.ChannelEntry.COLUMN_NAME_VALUE,
                SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE,
                SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID,

        };

        String selection = ( primary_key == true ? SuplaContract.ChannelEntry._ID : SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID ) + " = ?" +
                " AND " + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID +
                " IN ( SELECT "+SuplaContract.LocationEntry._ID+" FROM "+SuplaContract.LocationEntry.TABLE_NAME+" WHERE "+SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID+" = ? )";

        String[] selectionArgs = {
                String.valueOf(id),
                String.valueOf(accessid),
        };


        Cursor c = db.query(
                SuplaContract.ChannelEntry.TABLE_NAME,
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

    private void _updateChannel(SQLiteDatabase db, Channel channel) {

        String selection = SuplaContract.ChannelEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(channel.getId()) };

        db.update(
                SuplaContract.ChannelEntry.TABLE_NAME,
                channel.getContentValues(),
                selection,
                selectionArgs);
    }

    public boolean updateChannel(SuplaChannel suplaChannel) {

        long accessid = getCurrentAccessId();


        if ( accessid != 0 ) {

            Location location = getLocation(accessid, suplaChannel.LocationID, false);
            SQLiteDatabase db = null;

            if ( location != null ) {

                Channel channel = getChannel(accessid, suplaChannel.Id, false);

                if ( channel == null ) {

                    channel = new Channel();
                    channel.AssignSuplaChannel(suplaChannel);
                    channel.setVisible(1);
                    channel.setLocationId(location.getId());

                    db = getWritableDatabase();
                    db.insert(
                            SuplaContract.ChannelEntry.TABLE_NAME,
                            null,
                            channel.getContentValues());

                } else if ( channel.Diff(suplaChannel)
                            || channel.getVisible() != 1 ) {

                    db = getWritableDatabase();

                    channel.AssignSuplaChannel(suplaChannel);
                    channel.setVisible(1);

                    _updateChannel(db, channel);
                };


                if ( db != null ) {
                    db.close();
                    return true;
                }

            }
        }

        return false;
    }

    public boolean updateChannelValue(SuplaChannelValueUpdate channelValue) {

        long accessid = getCurrentAccessId();
        Channel channel = getChannel(accessid, channelValue.Id, false);

        if ( channel != null
                && ( channel.getOnLine() != channelValue.OnLine
                     || channel.getValue().Diff(channelValue.Value) ) ) {

            SQLiteDatabase db = getWritableDatabase();

            channel.setOnLine(channelValue.OnLine);
            channel.getValue().AssignSuplaChannelValue(channelValue.Value);


            _updateChannel(db, channel);
            db.close();

            return true;
        }

        return false;
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

        return count > 0 ? true : false;
    }

    public boolean setChannelsOffline() {

        String selection = SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE + " = ?";
        String[] selectionArgs = { String.valueOf((int)1) };

        ContentValues values = new ContentValues();
        values.put(SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE, 0);

        SQLiteDatabase db = getWritableDatabase();

        int count = db.update(
                SuplaContract.ChannelEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();

        return count > 0 ? true : false;
    }

    public int getChannelCount() {

        String selection = "SELECT count(*) FROM " + SuplaContract.ChannelEntry.TABLE_NAME +
                " WHERE " + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID +
                " IN ( SELECT "+SuplaContract.LocationEntry._ID+" FROM "+SuplaContract.LocationEntry.TABLE_NAME+" WHERE "+SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID+" = ? )";

        int count = 0;
        long accessid = getCurrentAccessId();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selection, new String[] {String.valueOf(accessid)});
        c.moveToFirst();
        count = c.getInt(0);
        c.close();
        db.close();

        return count;

    }

    public Cursor getChannelListCursor() {


        String sql = "SELECT "
                +"C."+    SuplaContract.ChannelEntry._ID
                +", L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION + " AS section"
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_CHANNELID
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_ONLINE
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_SUBVALUE
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_VALUE
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE
                +", C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID

                + " FROM " + SuplaContract.ChannelEntry.TABLE_NAME + " C"
                + " JOIN " + SuplaContract.LocationEntry.TABLE_NAME + " L"
                + " ON C." + SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID + " = L." + SuplaContract.LocationEntry._ID
                + " WHERE C." + SuplaContract.ChannelEntry.COLUMN_NAME_VISIBLE + " > 0"
                + " ORDER BY " + "L." + SuplaContract.LocationEntry.COLUMN_NAME_CAPTION+", "
                               + "C." + SuplaContract.ChannelEntry.COLUMN_NAME_FUNC+" DESC, "
                               + "C." + SuplaContract.ChannelEntry.COLUMN_NAME_CAPTION;



        return rdb.rawQuery(sql, null);
    }


    public ColorListItem getColorListItem(long channel, int idx) {

        ColorListItem result = null;
        SQLiteDatabase db = getReadableDatabase();


        String[] projection = {
                SuplaContract.ColorListItemEntry._ID,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNEL,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR,
                SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS,

        };

        String selection = "( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNEL + " = ?" +
                " AND " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ? )";

        String[] selectionArgs = {
                String.valueOf(channel),
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

        ColorListItem cli = getColorListItem(item.getChannel(), item.getIdx());

        SQLiteDatabase db = getWritableDatabase();

        if ( cli == null ) {

            db = getWritableDatabase();
            db.insert(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    null,
                    item.getContentValues());

        } else {

            cli.AssignColorListItem(item);

            String selection = "( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_CHANNEL + " = ? ) AND ( " + SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX + " = ? )";
            String[] selectionArgs = { String.valueOf(cli.getChannel()), String.valueOf(cli.getIdx()) };

            db.update(
                    SuplaContract.ColorListItemEntry.TABLE_NAME,
                    cli.getContentValues(),
                    selection,
                    selectionArgs);

        }


        db.close();
    }

}
