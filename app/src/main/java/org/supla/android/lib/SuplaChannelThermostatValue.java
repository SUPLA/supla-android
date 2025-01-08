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

import java.io.Serializable;
import java.util.Arrays;
import org.supla.android.tools.UsedFromNativeCode;

public class SuplaChannelThermostatValue implements Serializable {

  private Double[] MeasuredTemperature;
  private Double[] PresetTemperature;
  private Integer[] Flags;
  private Integer[] Values;
  private Schedule mSchedule;
  private Time mTime;

  SuplaChannelThermostatValue() {
    // This constructor is used by native code

    MeasuredTemperature = null;
    PresetTemperature = null;
    Values = null;
    Flags = null;
    mSchedule = null;
    mTime = null;
  }

  public Double getMeasuredTemperature(int idx) {
    return MeasuredTemperature != null && idx >= 0 && idx < MeasuredTemperature.length
        ? MeasuredTemperature[idx]
        : null;
  }

  @UsedFromNativeCode
  boolean setMeasuredTemperature(int idx, double temperature) {
    if (idx >= 0 && idx < 10) {
      if (MeasuredTemperature == null) {
        MeasuredTemperature = new Double[10];

        Arrays.fill(MeasuredTemperature, -273.0);
      }
      MeasuredTemperature[idx] = temperature;
      return true;
    }
    return false;
  }

  public Double getPresetTemperature(int idx) {
    return PresetTemperature != null && idx >= 0 && idx < PresetTemperature.length
        ? PresetTemperature[idx]
        : null;
  }

  @UsedFromNativeCode
  boolean setPresetTemperature(int idx, double temperature) {
    if (idx >= 0 && idx < 10) {

      if (PresetTemperature == null) {
        PresetTemperature = new Double[10];

        Arrays.fill(PresetTemperature, -273.0);
      }

      PresetTemperature[idx] = temperature;
      return true;
    }
    return false;
  }

  public Integer getFlags(int idx) {
    return Flags != null && idx >= 0 && idx < Flags.length ? Flags[idx] : null;
  }

  @UsedFromNativeCode
  boolean setFlags(int idx, int flags) {
    if (idx >= 0 && idx < 8) {
      if (Flags == null) {
        Flags = new Integer[8];
      }
      Flags[idx] = flags;
      return true;
    }
    return false;
  }

  public Integer getValues(int idx) {
    return Values != null && idx >= 0 && idx < Values.length ? Values[idx] : null;
  }

  @UsedFromNativeCode
  public boolean setValues(int idx, int values) {
    if (idx >= 0 && idx < 8) {
      if (Values == null) {
        Values = new Integer[8];
      }
      Values[idx] = values;
      return true;
    }
    return false;
  }

  @UsedFromNativeCode
  public void setScheduleValue(byte day, byte hour, byte value) {
    if (mSchedule == null) {
      mSchedule = new Schedule();
    }

    mSchedule.setValue(day, hour, value);
  }

  @UsedFromNativeCode
  public void setScheduleValueType(byte type) {
    if (mSchedule == null) {
      mSchedule = new Schedule();
    }

    mSchedule.setType(type);
  }

  public Schedule getSchedule() {
    return mSchedule;
  }

  public void setSchedule(Schedule schedule) {
    mSchedule = schedule;
  }

  @UsedFromNativeCode
  public void setTime(byte second, byte minute, byte hour, byte dayOfWeek) {
    mTime = new Time(second, minute, hour, dayOfWeek);
  }

  public Time getTime() {
    return mTime;
  }

  public void setTime(Time time) {
    mTime = time;
  }

  public static class Schedule implements Serializable {

    public static final byte TYPE_TEMPERATURE = 0;
    public static final byte TYPE_PROGRAM = 1;

    private byte Type;
    private final byte[][] HourValue;

    Schedule() {
      HourValue = new byte[7][24];
    }

    public boolean setValue(byte day, byte hour, byte value) {
      if (day >= 0 && day < HourValue.length && hour >= 0 && hour < HourValue[day].length) {
        HourValue[day][hour] = value;
        return true;
      }
      return false;
    }

    public byte getValue(byte day, byte hour) {
      return day >= 0 && day < HourValue.length && hour >= 0 && hour < HourValue[day].length
          ? HourValue[day][hour]
          : 0;
    }

    public byte getType() {
      return Type;
    }

    public void setType(byte type) {
      Type = type;
    }
  }

  public static class Time implements Serializable {
    private byte Second;
    private byte Minute;
    private byte Hour;
    private byte DayOfWeek;

    Time(byte second, byte minute, byte hour, byte dayOfWeek) {
      Second = second;
      Minute = minute;
      Hour = hour;
      DayOfWeek = dayOfWeek;
    }

    public byte getSecond() {
      return Second;
    }

    public void setSecond(byte second) {
      Second = second;
    }

    public byte getMinute() {
      return Minute;
    }

    public void setMinute(byte minute) {
      Minute = minute;
    }

    public byte getHour() {
      return Hour;
    }

    public void setHour(byte hour) {
      Hour = hour;
    }

    public byte getDayOfWeek() {
      return DayOfWeek;
    }

    public void setDayOfWeek(byte dayOfWeek) {
      DayOfWeek = dayOfWeek;
    }
  }
}
