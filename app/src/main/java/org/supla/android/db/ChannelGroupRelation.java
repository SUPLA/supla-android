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

import org.supla.android.lib.SuplaChannelGroupRelation;

public class ChannelGroupRelation extends DbItem {

    private int GroupId;
    private int ChannelId;
    private int Visible;
    private int profileId;

    public int getGroupId() {
        return GroupId;
    }

    public void setGroupId(int groupId) {
        GroupId = groupId;
    }

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public int getVisible() {
        return Visible;
    }

    public void setVisible(int visible) {
        Visible = visible;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int pid) {
        profileId = pid;
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getLong(cursor.getColumnIndex(SuplaContract.ChannelGroupRelationEntry._ID)));
        setGroupId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID)));
        setVisible(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE)));
        setProfileId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_PROFILEID)));
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_GROUPID, getGroupId());
        values.put(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_VISIBLE, getVisible());
        values.put(SuplaContract.ChannelGroupRelationEntry.COLUMN_NAME_PROFILEID, getProfileId());

        return values;

    }

    public void Assign(SuplaChannelGroupRelation cgrel) {
        setGroupId(cgrel.ChannelGroupID);
        setChannelId(cgrel.ChannelID);
    }
}
