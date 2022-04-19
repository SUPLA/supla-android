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

import org.json.JSONException;
import org.json.JSONObject;

public class ThermostatMeasurementItem extends MeasurementItem {

    private boolean On;
    private Double MeasuredTemperature;
    private Double PresetTemperature;
    private int profileId;

    public ThermostatMeasurementItem() {
        MeasuredTemperature = null;
        PresetTemperature = null;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int pid) {
        profileId = pid;
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

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ThermostatLogEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID)));

        setTimestamp(cursor.getLong(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP)));

        setOn(cursor.getInt(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_ON)) > 0);

        setMeasuredTemperature(cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE)));

        setPresetTemperature(cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE)));
        setProfileId(cursor.getInt(cursor.getColumnIndex(SuplaContract.ThermostatLogEntry.COLUMN_NAME_PROFILEID)));
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP, getTimestamp());
        values.put(SuplaContract.ThermostatLogEntry.COLUMN_NAME_ON, isOn() ? 1 : 0);

        putNullOrDouble(values,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE,
                getMeasuredTemperature());

        putNullOrDouble(values,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE,
                getPresetTemperature());
        values.put(SuplaContract.ThermostatLogEntry.COLUMN_NAME_PROFILEID, getProfileId());
        

        return values;
    }

}
