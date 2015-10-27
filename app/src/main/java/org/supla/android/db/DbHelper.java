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
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaLocation;

import java.nio.channels.Channel;

public class DbHelper extends SQLiteOpenHelper {

    private Context context;
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "supla.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        final String SQL_DROP_ACCESSID_TABLE = "DROP TABLE IF EXISTS "+SuplaContract.AccessIDEntry.TABLE_NAME;
        final String SQL_DROP_LOCATION_TABLE = "DROP TABLE IF EXISTS "+SuplaContract.LocationEntry.TABLE_NAME;
        final String SQL_DROP_CHANNEL_TABLE = "DROP TABLE IF EXISTS "+SuplaContract.ChannelEntry.TABLE_NAME;

        db.execSQL(SQL_DROP_ACCESSID_TABLE);
        db.execSQL(SQL_DROP_LOCATION_TABLE);
        db.execSQL(SQL_DROP_CHANNEL_TABLE);

        onCreate(db);
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
            result = new Location();
            result.AssignCursorData(c);
        }

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
                location.AssignSuplaLocation(suplaLocation);
                location.setVisible(1);
                location.setAccessId(accessid);

                db = getWritableDatabase();
                db.insert(
                        SuplaContract.LocationEntry.TABLE_NAME,
                        null,
                        location.getContentValues());


            } else if ( location.Diff(suplaLocation) ) {

                db = getWritableDatabase();

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

        return null;
    }

    public boolean updateChannel(SuplaChannel channel) {
        long accessid = getCurrentAccessId();

        return false;
    }

    public boolean updateChannelValue(SuplaChannelValue channelValue) {

        return false;
    }
}
