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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity;
import org.supla.android.lib.SuplaChannelGroupRelation;

public class ChannelGroupRelation extends DbItem {

  private int GroupId;
  private int ChannelId;
  private int Visible;
  private long profileId;

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

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long pid) {
    profileId = pid;
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {

    setId(cursor.getLong(cursor.getColumnIndex(ChannelGroupRelationEntity.COLUMN_ID)));
    setGroupId(cursor.getInt(cursor.getColumnIndex(ChannelGroupRelationEntity.COLUMN_GROUP_ID)));
    setChannelId(
        cursor.getInt(cursor.getColumnIndex(ChannelGroupRelationEntity.COLUMN_CHANNEL_ID)));
    setVisible(cursor.getInt(cursor.getColumnIndex(ChannelGroupRelationEntity.COLUMN_VISIBLE)));
    setProfileId(
        cursor.getLong(cursor.getColumnIndex(ChannelGroupRelationEntity.COLUMN_PROFILE_ID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(ChannelGroupRelationEntity.COLUMN_GROUP_ID, getGroupId());
    values.put(ChannelGroupRelationEntity.COLUMN_CHANNEL_ID, getChannelId());
    values.put(ChannelGroupRelationEntity.COLUMN_VISIBLE, getVisible());
    values.put(ChannelGroupRelationEntity.COLUMN_PROFILE_ID, getProfileId());

    return values;
  }

  public void Assign(SuplaChannelGroupRelation cgrel) {
    setGroupId(cgrel.ChannelGroupID);
    setChannelId(cgrel.ChannelID);
  }
}
