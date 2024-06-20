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
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity;
import org.supla.android.lib.SuplaChannelExtendedValue;

public class ChannelExtendedValue extends DbItem {
  private int ChannelId;
  private long profileId;
  private Long timerStartTimestamp;

  private SuplaChannelExtendedValue ExtendedValue;

  public static boolean valueExists(Cursor cursor) {
    int vidx = cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_VALUE);

    return cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_ID) > -1
        && cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_CHANNEL_ID) > -1
        && vidx > -1
        && !cursor.isNull(vidx);
  }

  public int getChannelId() {
    return ChannelId;
  }

  public void setChannelId(int channelId) {
    ChannelId = channelId;
  }

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long pid) {
    profileId = pid;
  }

  @Nullable
  public Long getTimerStartTimestamp() {
    return timerStartTimestamp;
  }

  public void setTimerStartTimestamp(@Nullable Long timestamp) {
    timerStartTimestamp = timestamp;
  }

  public SuplaChannelExtendedValue getExtendedValue() {
    return ExtendedValue;
  }

  public Date getTimerEstimatedEndDate() {
    if (ExtendedValue == null) {
      return null;
    }
    if (ExtendedValue.TimerStateValue == null) {
      return null;
    }

    return ExtendedValue.TimerStateValue.getCountdownEndsAt();
  }

  public void setExtendedValue(SuplaChannelExtendedValue extendedValue) {
    ExtendedValue = extendedValue;
  }

  private Object ByteArrayToObject(byte[] value) {
    try {
      ByteArrayInputStream byteStream = new ByteArrayInputStream(value);
      ObjectInputStream objectStream = new ObjectInputStream(byteStream);
      return objectStream.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return null;
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {
    setId(cursor.getLong(cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_ID)));
    setChannelId(
        cursor.getInt(cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_CHANNEL_ID)));
    setProfileId(
        cursor.getLong(cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_PROFILE_ID)));
    try {
      setTimerStartTimestamp(
          cursor.getLong(
              cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME)));
    } catch (Exception ex) {
      setTimerStartTimestamp(null);
    }

    byte[] value = cursor.getBlob(cursor.getColumnIndex(ChannelExtendedValueEntity.COLUMN_VALUE));
    Object obj = ByteArrayToObject(value);

    if (obj instanceof SuplaChannelExtendedValue) {
      ExtendedValue = (SuplaChannelExtendedValue) obj;
    } else {
      ExtendedValue = new SuplaChannelExtendedValue();
    }
  }

  private byte[] ObjectToByteArray(Object obj) {
    byte[] value = new byte[0];
    try {
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
      objectStream.writeObject(obj);
      objectStream.close();
      value = byteStream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return value;
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(ChannelExtendedValueEntity.COLUMN_CHANNEL_ID, getChannelId());
    values.put(ChannelExtendedValueEntity.COLUMN_VALUE, ObjectToByteArray(ExtendedValue));
    values.put(ChannelExtendedValueEntity.COLUMN_PROFILE_ID, getProfileId());
    values.put(ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME, getTimerStartTimestamp());

    return values;
  }
}
