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

public class TemperatureMeasurementItem extends MeasurementItem {

    private Double Temperature;
    private int profileId;

    public TemperatureMeasurementItem() {
        Temperature = null;
    }

    public int getProfileId() {
        return profileId;
    }


    public void setProfileId(int pid) {
        profileId = pid;
    }

    public Double getTemperature() {
        return Temperature;
    }

    public void setTemperature(Double temperature) {
        Temperature = temperature;
    }

    public void AssignJSONObject(JSONObject obj) throws JSONException {
        setTimestamp(obj.getLong("date_timestamp"));
        setTemperature(getTemperature(obj, "temperature"));
    }

    public void AssignCursorData(Cursor cursor) {

        setId(cursor.getInt(cursor.getColumnIndex(SuplaContract.TemperatureLogEntry._ID)));
        setChannelId(cursor.getInt(cursor.getColumnIndex(
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID)));

        setTimestamp(cursor.getLong(cursor.getColumnIndex(
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP)));

        setTemperature(cursor.getDouble(cursor.getColumnIndex(
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE)));

        setProfileId(cursor.getInt(cursor.getColumnIndex(SuplaContract.TemperatureLogEntry.COLUMN_NAME_PROFILEID)));
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, getChannelId());
        values.put(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP, getTimestamp());

        putNullOrDouble(values,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE,
                getTemperature());
        values.put(SuplaContract.TemperatureLogEntry.COLUMN_NAME_PROFILEID, getProfileId());

        return values;
    }

}
