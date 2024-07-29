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
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity;

public class ThermostatMeasurementItem extends MeasurementItem {

  private boolean On;
  private Double MeasuredTemperature;
  private Double PresetTemperature;

  public ThermostatMeasurementItem() {
    MeasuredTemperature = null;
    PresetTemperature = null;
  }

  public boolean isOn() {
    return On;
  }

  public void setOn(boolean on) {
    On = on;
  }

  public Double getMeasuredTemperature() {
    return MeasuredTemperature;
  }

  public void setMeasuredTemperature(Double measuredTemperature) {
    MeasuredTemperature = measuredTemperature;
  }

  public Double getPresetTemperature() {
    return PresetTemperature;
  }

  public void setPresetTemperature(Double presetTemperature) {
    PresetTemperature = presetTemperature;
  }

  public void AssignJSONObject(JSONObject obj) throws JSONException {
    setTimestamp(obj.getLong("date_timestamp"));
    setOn(getBoolean(obj, "on"));
    setMeasuredTemperature(getTemperature(obj, "measured_temperature"));
    setPresetTemperature(getTemperature(obj, "preset_temperature"));
  }

  @SuppressLint("Range")
  public void AssignCursorData(Cursor cursor) {

    setId(cursor.getLong(cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_ID)));
    setChannelId(
        cursor.getInt(cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID)));

    setTimestamp(
        cursor.getLong(cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_TIMESTAMP)));

    setOn(cursor.getInt(cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_IS_ON)) > 0);

    setMeasuredTemperature(
        cursor.getDouble(
            cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_MEASURED_TEMPERATURE)));

    setPresetTemperature(
        cursor.getDouble(
            cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_PRESET_TEMPERATURE)));
    setProfileId(
        cursor.getLong(cursor.getColumnIndex(HomePlusThermostatLogEntity.COLUMN_PROFILE_ID)));
  }

  public ContentValues getContentValues() {

    ContentValues values = new ContentValues();

    values.put(HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID, getChannelId());
    values.put(HomePlusThermostatLogEntity.COLUMN_TIMESTAMP, getTimestamp());
    values.put(HomePlusThermostatLogEntity.COLUMN_IS_ON, isOn() ? 1 : 0);

    putNullOrDouble(
        values, HomePlusThermostatLogEntity.COLUMN_MEASURED_TEMPERATURE, getMeasuredTemperature());

    putNullOrDouble(
        values, HomePlusThermostatLogEntity.COLUMN_PRESET_TEMPERATURE, getPresetTemperature());
    values.put(HomePlusThermostatLogEntity.COLUMN_PROFILE_ID, getProfileId());

    return values;
  }
}
