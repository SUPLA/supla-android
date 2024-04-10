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

  private int BufferOnLineCount;
  private int BufferOnLine;
  private int BufferCounter;
  private String BufferTotalValue;
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

  public boolean DiffWithBuffer() {

    return OnLine != BufferOnLine || TotalValue == null || !TotalValue.equals(BufferTotalValue);
  }

  public void resetBuffer() {
    BufferTotalValue = "";
    BufferOnLine = 0;
    BufferOnLineCount = 0;
    BufferCounter = 0;
  }

  public void assignBuffer() {

    OnLine = BufferOnLine;
    TotalValue = BufferTotalValue;

    resetBuffer();
  }

  public void addValueToBuffer(ChannelValue value) {

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
      case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        break;
      default:
        return;
    }

    BufferCounter++;
    if (value.getOnLine()) {
      BufferOnLineCount++;
    }

    BufferOnLine = BufferOnLineCount * 100 / BufferCounter;

    if (!value.getOnLine()) {
      return;
    }

    if (!BufferTotalValue.isEmpty()) {
      BufferTotalValue += "|";
    }

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
        BufferTotalValue += Integer.toString((value.getSubValueHi() & 0x1) == 0x1 ? 1 : 0);
        break;
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
      case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        BufferTotalValue += Integer.toString(value.hiValue() ? 1 : 0);
        break;
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
        BufferTotalValue += Integer.toString(value.getRollerShutterValue().getClosingPercentage());
        BufferTotalValue += ":";
        BufferTotalValue += Integer.toString((value.getSubValueHi() & 0x1) == 0x1 ? 1 : 0);
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        BufferTotalValue += Integer.toString(value.getBrightness());
        break;
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        BufferTotalValue += Integer.toString(value.getColor());
        BufferTotalValue += ":";
        BufferTotalValue += Integer.toString(value.getColorBrightness());
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        BufferTotalValue += Integer.toString(value.getColor());
        BufferTotalValue += ":";
        BufferTotalValue += Integer.toString(value.getColorBrightness());
        BufferTotalValue += ":";
        BufferTotalValue += Integer.toString(value.getBrightness());
        break;
      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        BufferTotalValue += Integer.toString(value.hiValue() ? 1 : 0);
        BufferTotalValue += ":";
        BufferTotalValue += Double.toString(value.getMeasuredTemp(getFunc()));
        BufferTotalValue += ":";
        BufferTotalValue += Double.toString(value.getPresetTemp(getFunc()));
        break;
    }
  }

  public ArrayList<Integer> getIntegersFromTotalValue() {
    ArrayList<Integer> result = new ArrayList<>();
    String[] items = getTotalValue().split("|");

    for (int a = 0; a < items.length; a++) {
      try {
        result.add(Integer.valueOf(items[a]));
      } catch (NumberFormatException e) {
      }
    }

    return result;
  }

  public ArrayList<Float> getRollerShutterPositions() {
    ArrayList<Float> result = new ArrayList<>();
    String[] items = getTotalValue().split("\\|");

    for (int a = 0; a < items.length; a++) {
      String[] n = items[a].split(":");
      if (n.length == 2) {
        try {
          float pos = Integer.valueOf(n[0]).intValue();
          int sensor = Integer.valueOf(n[1]).intValue();

          if (pos < 100 && sensor == 1) {
            pos = 100;
          }

          result.add(Float.valueOf(pos));
        } catch (NumberFormatException e) {
        }
      }
    }

    return result;
  }

  public ArrayList<Double> getDoubleValues() {

    ArrayList<Double> result = new ArrayList<>();
    String[] items = getTotalValue().split("\\|");

    for (int a = 0; a < items.length; a++) {

      try {
        result.add(Double.valueOf(items[a]));
      } catch (NumberFormatException e) {
      }
    }
    return result;
  }

  private ArrayList<Double> getRGBWValues(int idx) {

    ArrayList<Double> result = new ArrayList<>();
    String[] items = getTotalValue().split("\\|");

    for (int a = 0; a < items.length; a++) {
      String[] n = items[a].split(":");
      if (idx < n.length) {
        try {
          result.add(Double.valueOf(n[idx]));
        } catch (NumberFormatException e) {
        }
      }
    }

    return result;
  }

  public ArrayList<Double> getColors() {
    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        return getRGBWValues(0);
    }
    return null;
  }

  public ArrayList<Double> getColorBrightness() {
    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        return getRGBWValues(1);
    }
    return null;
  }

  public ArrayList<Double> getBrightness() {
    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        return getDoubleValues();
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        return getRGBWValues(2);
    }
    return null;
  }

  private Double getMinMaxTemperature(boolean preset, boolean min) {
    Double result = null;
    String[] items = getTotalValue().split("\\|");
    int idx = preset ? 2 : 1;

    for (int a = 0; a < items.length; a++) {
      String[] n = items[a].split(":");
      if (idx < n.length) {
        try {
          Double v = Double.valueOf(n[idx]);

          if (result != null) {
            if ((min && v.doubleValue() < result.doubleValue())
                || (!min && v.doubleValue() > result.doubleValue())) {
              result = v;
            }
          } else {
            result = v;
          }

        } catch (NumberFormatException e) {
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

  public int getActivePercent(int idx) {
    String[] items = getTotalValue().split("\\|");

    int sum = 0;
    int count = 0;
    String[] n;

    for (int a = 0; a < items.length; a++) {

      switch (getFunc()) {
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
        case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
        case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
        case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
        case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
          try {
            sum += Integer.valueOf(items[a]).intValue() > 0 ? 1 : 0;
          } catch (NumberFormatException e) {
          }
          count++;
          break;
        case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
          n = items[a].split(":");
          if (n.length == 2) {
            try {
              sum += Integer.valueOf(n[1]).intValue() > 0 ? 1 : 0;
            } catch (NumberFormatException e) {
            }
          }

          count++;
          break;
        case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
          n = items[a].split(":");
          if (n.length == 3) {
            try {
              if (idx == 0 || idx == 1) {
                sum += Integer.valueOf(n[1]).intValue() > 0 ? 1 : 0;
              }

              if (idx == 0 || idx == 2) {
                sum += Integer.valueOf(n[2]).intValue() > 0 ? 1 : 0;
              }

            } catch (NumberFormatException e) {
            }
          }

          if (idx == 0) {
            count += 2;
          } else {
            count++;
          }

          break;

        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
          n = items[a].split(":");
          if (n.length == 2) {
            try {
              if (Integer.valueOf(n[0]).intValue() >= 100 // percent
                  || Integer.valueOf(n[1]).intValue() > 0) { // sensor
                sum++;
              }
            } catch (NumberFormatException e) {
            }
          }
          count++;
          break;

        case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
          n = items[a].split(":");
          if (n.length == 3) {
            try {
              sum += Integer.valueOf(n[0]).intValue() > 0 ? 1 : 0;
            } catch (NumberFormatException e) {
            }
          }

          count++;
          break;
      }
    }

    return count == 0 ? 0 : sum * 100 / count;
  }

  public int getActivePercent() {
    return getActivePercent(0);
  }

  public CharSequence getHumanReadableValue(WhichOne whichOne) {
    return null;
  }

  public CharSequence getHumanReadableValue() {
    if (getFunc() == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS) {
      return getHumanReadableThermostatTemperature(
          getMinimumMeasuredTemperature(),
          getMaximumMeasuredTemperature(),
          getMinimumPresetTemperature(),
          getMaximumPresetTemperature(),
          0.8f,
          0.6f);
    }
    return null;
  }
}
