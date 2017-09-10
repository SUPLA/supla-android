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

import org.supla.android.lib.SuplaLocation;

public class Location {
    private long Id;
    private int LocationId;
    private String Caption;
    private int Visible;
    private long AccessId;

    public void setId(long id) {
        Id = id;
    }

    public long getId() {
        return Id;
    }

    public void setLocationId(int locationId) {
        LocationId = locationId;
    }

    public int getLocationId() {
        return LocationId;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getCaption() {
        return Caption;
    }

    public void setVisible(int visible) {
        Visible = visible;
    }

    public int getVisible() {
        return Visible;
    }

    public void setAccessId(long accessId) {
        AccessId = accessId;
    }

    public long getAccessId() {
        return AccessId;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.LocationEntry._ID)));
        setLocationId(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_CAPTION)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE)));
        setAccessId(cursor.getLong(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID)));

    }

    public void AssignSuplaLocation(SuplaLocation location) {

        setLocationId(location.Id);
        setCaption(location.Caption);

    }

    public boolean Diff(SuplaLocation location) {

        return location.Id != getLocationId() || location.Caption.equals(getCaption()) == false;

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_ACCESSID, getAccessId());

        return values;
    }
}
