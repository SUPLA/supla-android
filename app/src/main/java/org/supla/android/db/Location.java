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
import android.annotation.SuppressLint;

import org.supla.android.lib.SuplaLocation;

import java.util.Arrays;

public class Location extends DbItem {

    private int LocationId;
    private String Caption;
    private int Visible;
    private int collapsed; // 0 - channels visible
    // 0x1 - channels collapsed
    // 0x2 - channel groups collapsed
    // 0x4 - scenes collapsed
    private SortingType sorting;
    private int sortOrder;
    private long profileId;

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long pid) {
        profileId = pid;
    }

    
    public int getLocationId() {
        return LocationId;
    }

    public void setLocationId(int locationId) {
        LocationId = locationId;
    }

    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public int getVisible() {
        return Visible;
    }

    public void setVisible(int visible) {
        Visible = visible;
    }

    public int getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(int collapsed) {
        this.collapsed = collapsed;
    }

    public SortingType getSorting() {
        return sorting;
    }

    public void setSorting(SortingType sorting) {
        this.sorting = sorting;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int s) {
        sortOrder = s;
    }

    @SuppressLint("Range")
    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.LocationEntry._ID)));
        setLocationId(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID)));
        setCaption(cursor.getString(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_CAPTION)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE)));
        setCollapsed(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED)));
        setSorting(SortingType.fromString(cursor.getString(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_SORTING))));
        setSortOrder(cursor.getInt(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER)));
        setProfileId(cursor.getLong(cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID)));
    }

    public void AssignSuplaLocation(SuplaLocation location) {

        setLocationId(location.Id);
        setCaption(location.Caption);
        // NOTE: profileId assigned by caller

    }

    public boolean Diff(SuplaLocation location) {

        return location.Id != getLocationId() || !location.Caption.equals(getCaption());

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.LocationEntry.COLUMN_NAME_LOCATIONID, getLocationId());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_CAPTION, getCaption());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED, getCollapsed());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_SORTING, getSorting().name());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_SORT_ORDER, getSortOrder());
        values.put(SuplaContract.LocationEntry.COLUMN_NAME_PROFILEID, getProfileId());

        return values;
    }

    public enum SortingType {
        DEFAULT, USER_DEFINED;

        public static SortingType fromString(String text) {
            for (SortingType sortingType : values()) {
                if (sortingType.name().equals(text)) {
                    return sortingType;
                }
            }

            return DEFAULT;
        }
    }
}
