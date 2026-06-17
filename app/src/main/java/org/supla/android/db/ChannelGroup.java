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
import java.util.ArrayList;
import org.supla.android.data.source.local.entity.ChannelGroupEntity;
import org.supla.android.lib.SuplaConst;

public class ChannelGroup extends ChannelBase {

  private String TotalValue;
  private int OnLine;

  private int position;

  protected int _getOnLine() {
    return OnLine;
  }

  public int getGroupId() {
    return getRemoteId();
  }

  public void setTotalValue(String totalValue) {
    this.TotalValue = totalValue;
  }

  public String getTotalValue() {
    return TotalValue == null ? "" : TotalValue;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  public void setOnline(int online) {
    this.OnLine = online;
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {
    setId(cursor.getLong(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_ID)));
    setRemoteId(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_REMOTE_ID)));
    setFunc(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_FUNCTION)));
    setVisible(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_VISIBLE)));
    OnLine = cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_ONLINE));
    setCaption(cursor.getString(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_CAPTION)));
    TotalValue = cursor.getString(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_TOTAL_VALUE));
    setLocationId(cursor.getLong(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_LOCATION_ID)));
    setAltIcon(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_ALT_ICON)));
    setUserIconId(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_USER_ICON)));
    setFlags(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_FLAGS)));
    setPosition(cursor.getInt(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_POSITION)));
    setProfileId(cursor.getLong(cursor.getColumnIndex(ChannelGroupEntity.COLUMN_PROFILE_ID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(ChannelGroupEntity.COLUMN_REMOTE_ID, getRemoteId());
    values.put(ChannelGroupEntity.COLUMN_CAPTION, getCaption());
    values.put(ChannelGroupEntity.COLUMN_TOTAL_VALUE, getTotalValue());
    values.put(ChannelGroupEntity.COLUMN_ONLINE, getOnLinePercent());
    values.put(ChannelGroupEntity.COLUMN_FUNCTION, getFunc());
    values.put(ChannelGroupEntity.COLUMN_VISIBLE, getVisible());
    values.put(ChannelGroupEntity.COLUMN_LOCATION_ID, getLocationId());
    values.put(ChannelGroupEntity.COLUMN_ALT_ICON, getAltIcon());
    values.put(ChannelGroupEntity.COLUMN_USER_ICON, getUserIconId());
    values.put(ChannelGroupEntity.COLUMN_FLAGS, getFlags());
    values.put(ChannelGroupEntity.COLUMN_POSITION, position);
    values.put(ChannelGroupEntity.COLUMN_PROFILE_ID, getProfileId());

    return values;
  }

  public ArrayList<Double> getDoubleValues() {

    ArrayList<Double> result = new ArrayList<>();
    String[] items = getTotalValue().split("\\|");

    for (String item : items) {
      try {
        result.add(Double.valueOf(item));
      } catch (NumberFormatException e) {
        // Skip if fails
      }
    }
    return result;
  }

  private ArrayList<Double> getRGBWValues(int idx) {

    ArrayList<Double> result = new ArrayList<>();
    String[] items = getTotalValue().split("\\|");

    for (String item : items) {
      String[] n = item.split(":");
      if (idx < n.length) {
        try {
          result.add(Double.valueOf(n[idx]));
        } catch (NumberFormatException e) {
          // Skip if fails
        }
      }
    }

    return result;
  }

  public ArrayList<Double> getColors() {
    return switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING, SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> getRGBWValues(0);
      default -> null;
    };
  }

  public ArrayList<Double> getColorBrightness() {
    return switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING, SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> getRGBWValues(1);
      default -> null;
    };
  }

  public ArrayList<Double> getBrightness() {
    return switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER -> getDoubleValues();
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING -> getRGBWValues(2);
      default -> null;
    };
  }

  private Double getMinMaxTemperature(boolean preset, boolean min) {
    Double result = null;
    String[] items = getTotalValue().split("\\|");
    int idx = preset ? 2 : 1;

    for (String item : items) {
      String[] n = item.split(":");
      if (idx < n.length) {
        try {
          double v = Double.parseDouble(n[idx]);

          if (result != null) {
            if ((min && v < result) || (!min && v > result)) {
              result = v;
            }
          } else {
            result = v;
          }

        } catch (NumberFormatException e) {
          // Skip if fails
        }
      }
    }
    return result;
  }

  public Double getMinimumMeasuredTemperature() {
    return getMinMaxTemperature(false, true);
  }

  public Double getMaximumMeasuredTemperature() {
    return getMinMaxTemperature(false, false);
  }

  public Double getMinimumPresetTemperature() {
    return getMinMaxTemperature(true, true);
  }

  public Double getMaximumPresetTemperature() {
    return getMinMaxTemperature(true, false);
  }
}
