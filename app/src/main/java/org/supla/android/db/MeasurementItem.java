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
import org.json.JSONException;
import org.json.JSONObject;

public abstract class MeasurementItem extends DbItem {
  protected int ChannelId;
  protected long Timestamp;
  private long profileId;

  public MeasurementItem() {
    ChannelId = 0;
    Timestamp = 0;
  }

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long pid) {
    profileId = pid;
  }

  public int getChannelId() {
    return ChannelId;
  }

  public void setChannelId(int channelId) {
    ChannelId = channelId;
  }

  public long getTimestamp() {
    return Timestamp;
  }

  public void setTimestamp(long timestamp) {
    Timestamp = timestamp;
  }

  protected Double getDouble(JSONObject obj, String name) throws JSONException {
    if (!obj.isNull(name)) {
      return obj.getDouble(name);
    }
    return null;
  }

  protected boolean getBoolean(JSONObject obj, String name) throws JSONException {
    boolean result = false;

    if (!obj.isNull(name)) {
      try {
        result = obj.getBoolean(name);
      } catch (JSONException e) {
        result = obj.getInt(name) > 0;
      }
    }
    return result;
  }

  protected Double getTemperature(JSONObject obj, String name) throws JSONException {
    Double result = getDouble(obj, name);
    return result != null && result > -273 ? result : null;
  }

  protected void putNullOrDouble(ContentValues values, String name, Double value) {
    if (value == null) {
      values.putNull(name);
    } else {
      values.put(name, value);
    }
  }
}
