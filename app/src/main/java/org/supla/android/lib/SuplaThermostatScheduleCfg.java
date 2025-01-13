package org.supla.android.lib;

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

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public class SuplaThermostatScheduleCfg {

  ArrayList<CfgGroup> mGroups = new ArrayList<>();

  private void setHourValue(
      @HourValueType int type, @DayOfWeek int weekday, short hour, byte value) {
    if (hour < 0) {
      hour = 0;
    } else if (hour > 23) {
      hour = 23;
    }

    CfgGroup group;
    byte[] hourValue = new byte[24];
    int a;

    for (a = 0; a < mGroups.size(); a++) {
      group = mGroups.get(a);
      if ((group.mWeekDays & weekday) > 0) {
        group.mWeekDays ^= weekday;
        hourValue = group.mHourValue.clone();

        if (group.mWeekDays == 0) {
          mGroups.remove(a);
        }
        break;
      }
    }

    hourValue[hour] = value;

    for (a = 0; a < mGroups.size(); a++) {
      group = mGroups.get(a);
      if (group.mValueType == type && Arrays.equals(group.mHourValue, hourValue)) {
        group.mWeekDays |= weekday;
        break;
      }
    }

    if (a >= mGroups.size()) {
      group = new CfgGroup();
      group.mWeekDays = weekday;
      group.mHourValue = hourValue;
      group.mValueType = type;
      mGroups.add(group);
    }
  }

  public @DayOfWeek int getWeekdayByDayIndex(short idx) {
    switch (idx) {
      case 2:
        return DayOfWeek.MONDAY;
      case 3:
        return DayOfWeek.TUESDAY;
      case 4:
        return DayOfWeek.WEDNESDAY;
      case 5:
        return DayOfWeek.THURSDAY;
      case 6:
        return DayOfWeek.FRIDAY;
      case 7:
        return DayOfWeek.SATURDAY;
      default:
        return DayOfWeek.SUNDAY;
    }
  }

  public void setTemperature(@DayOfWeek int weekday, short hour, byte temperature) {
    setHourValue(HourValueType.TEMPERATURE, weekday, hour, temperature);
  }

  public void setProgram(@DayOfWeek int weekday, short hour, byte program) {
    setHourValue(HourValueType.PROGRAM, weekday, hour, program);
  }

  public void clear() {
    mGroups.clear();
  }

  public int getGroupCount() {
    return mGroups.size();
  }

  public int getGroupWeekDays(int groupIdx) {
    if (groupIdx >= 0 && groupIdx < mGroups.size()) {
      return mGroups.get(groupIdx).mWeekDays;
    }

    return 0;
  }

  public int getGroupHourValueType(int groupIdx) {
    if (groupIdx >= 0 && groupIdx < mGroups.size()) {
      return mGroups.get(groupIdx).mValueType;
    }

    return HourValueType.TEMPERATURE;
  }

  public byte[] getGroupHourValue(int groupIdx) {
    if (groupIdx >= 0 && groupIdx < mGroups.size()) {
      return mGroups.get(groupIdx).mHourValue;
    }
    return new byte[24];
  }

  public byte getGroupHourValue(int groupIdx, short hour) {
    if (groupIdx >= 0 && groupIdx < mGroups.size() && hour >= 0 && hour < 24) {
      return mGroups.get(groupIdx).mHourValue[hour];
    }
    return 0;
  }

  @IntDef({
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface DayOfWeek {
    int SUNDAY = 0x1;
    int MONDAY = 0x2;
    int TUESDAY = 0x4;
    int WEDNESDAY = 0x8;
    int THURSDAY = 0x10;
    int FRIDAY = 0x20;
    int SATURDAY = 0x40;
  }

  @IntDef({HourValueType.TEMPERATURE, HourValueType.PROGRAM})
  @Retention(RetentionPolicy.SOURCE)
  @interface HourValueType {
    int TEMPERATURE = 0;
    int PROGRAM = 1;
  }

  private class CfgGroup {
    public int mWeekDays = 0;
    public @HourValueType int mValueType;
    public byte[] mHourValue = new byte[24];
  }
}
