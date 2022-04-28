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
import org.supla.android.db.TemperatureMeasurementItem;

import java.util.Date;

public class TemperatureLogDao extends MeasurementsBaseDao {

    public TemperatureLogDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public int getTemperatureMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getTemperatureMeasurementTotalCount(int channelId) {
        return getCount(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                key(SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.TemperatureLogEntry.COLUMN_NAME_PROFILEID, getCurrentProfileId()));
    }

    public void deleteTemperatureMeasurements(int channelId) {
        delete(SuplaContract.TemperatureLogEntry.TABLE_NAME,
                key(SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.TemperatureLogEntry.COLUMN_NAME_PROFILEID, getCurrentProfileId()));
    }

    public void addTemperatureMeasurement(TemperatureMeasurementItem emi) {
        insert(emi, SuplaContract.TemperatureLogEntry.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getTemperatureMeasurements(int channelId, Date dateFrom, Date dateTo) {

        StringBuilder sqlBuilder = new StringBuilder().append("SELECT ")
                .append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE).append(", ")
                .append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP)
                .append(" FROM ").append(SuplaContract.TemperatureLogEntry.TABLE_NAME)
                .append(" WHERE ")
                .append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID).append(" = ").append(channelId);

        if (dateFrom != null && dateTo != null) {
            sqlBuilder.append(" AND ")
                    .append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP)
                    .append(" >= ").append(toMilis(dateFrom))
                    .append(" AND ")
                    .append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP)
                    .append(" <= ").append(toMilis(dateTo));
        }

        sqlBuilder.append(" ORDER BY ").append(SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP).append(" ASC ");

        return read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sqlBuilder.toString(), null));
    }
}
