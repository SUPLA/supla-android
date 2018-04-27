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

public class ColorListItem {

    private long Id;
    private int RemoteId;
    private boolean Group;
    private int Idx;
    private int Color;
    private short Brightness;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public int getRemoteId() {
        return RemoteId;
    }

    public void setRemoteId(int remoteId) {
        RemoteId = remoteId;
    }

    public boolean getGroup() { return Group; };

    public void setGroup(boolean group) { Group = group; };

    public int getIdx() {
        return Idx;
    }

    public void setIdx(int idx) {
        Idx = idx;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {
        Color = color;
    }

    public short getBrightness() {
        return Brightness;
    }

    public void setBrightness(short brightness) {
        Brightness = brightness;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ColorListItemEntry._ID)));
        setRemoteId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID)));
        setGroup(cursor.getInt(cursor.getColumnIndex(SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP)) > 0);
        setIdx(cursor.getInt(cursor.getColumnIndex(SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX)));
        setColor(cursor.getInt(cursor.getColumnIndex(SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR)));
        setBrightness(cursor.getShort(cursor.getColumnIndex(SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS)));
    }

    public void AssignColorListItem(ColorListItem cli) {

        setId(cli.getId());
        setRemoteId(cli.getRemoteId());
        setGroup(cli.getGroup());
        setIdx(cli.getIdx());
        setColor(cli.getColor());
        setBrightness(cli.getBrightness());

    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ColorListItemEntry.COLUMN_NAME_REMOTEID, getRemoteId());
        values.put(SuplaContract.ColorListItemEntry.COLUMN_NAME_GROUP, getGroup() ? 1 : 0);
        values.put(SuplaContract.ColorListItemEntry.COLUMN_NAME_IDX, getIdx());
        values.put(SuplaContract.ColorListItemEntry.COLUMN_NAME_COLOR, getColor());
        values.put(SuplaContract.ColorListItemEntry.COLUMN_NAME_BRIGHTNESS, getBrightness());

        return values;
    }
}
