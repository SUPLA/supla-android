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
import android.database.Cursor;

public class ChannelGroup extends ChannelBase {

    protected int _getOnLine() {
        return 0;
    }

    protected void setOnLine(int onLine) {
    }

    public int getGroupId() {
        return getRemoteId();
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry._ID)));
        setRemoteId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID)));
        setFunc(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC)));
        setOnLine(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE)));
        setLocationId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID)));
        setAltIcon(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON)));
        setFlags(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS)));

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_GROUPID, getRemoteId());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ONLINE, getOnLinePercent());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FUNC, getFunc());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_ALTICON, getAltIcon());
        values.put(SuplaContract.ChannelGroupEntry.COLUMN_NAME_FLAGS, getFlags());

        return values;

    }
}
