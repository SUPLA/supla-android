package org.supla.android.data.source.local;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import org.supla.android.db.SuplaContract;
import org.supla.android.db.ThermostatMeasurementItem;

public class ThermostatLogDao extends MeasurementsBaseDao {

    public ThermostatLogDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public int getThermostatMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getThermostatMeasurementTotalCount(int channelId) {
        return getCount(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                key(SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ThermostatLogEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public void deleteThermostatMeasurements(int channelId) {
        delete(SuplaContract.ThermostatLogEntry.TABLE_NAME,
                key(SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ThermostatLogEntry.COLUMN_NAME_PROFILEID, getCachedProfileId()));
    }

    public void addThermostatMeasurement(ThermostatMeasurementItem emi) {
        insert(emi, SuplaContract.ThermostatLogEntry.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getThermostatMeasurements(int channelId) {
        String sql = "SELECT "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP
                + " FROM " + SuplaContract.ThermostatLogEntry.TABLE_NAME
                + " WHERE " + SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + " = " + channelId
                + " ORDER BY " + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + " ASC ";

        return read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null));
    }
}
