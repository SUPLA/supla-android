package org.supla.android.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

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

        setMeasuredTemperature(cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE)));
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

        return values;
    }

}
