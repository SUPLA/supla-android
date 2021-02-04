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
import android.support.annotation.NonNull;

import org.supla.android.db.ImpulseCounterMeasurementItem;
import org.supla.android.db.SuplaContract;

import java.util.Calendar;
import java.util.Date;

public class ImpulseCounterLogDao extends MeasurementsBaseDao {
    public ImpulseCounterLogDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public double getLastImpulseCounterMeasurementValue(int monthOffset, int channelId) {
        return getLastMeasurementValue(SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE,
                monthOffset, channelId);
    }

    public void addImpulseCounterMeasurement(ImpulseCounterMeasurementItem item) {
        insert(item, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int getImpulseCounterMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public boolean impulseCounterMeasurementsStartsWithTheCurrentMonth(int channelId) {
        long minTS = getImpulseCounterMeasurementTimestamp(channelId, true);
        return timestampStartsWithTheCurrentMonth(minTS);
    }

    public int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement) {
        if (withoutComplement) {
            return getCount(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                    key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId),
                    key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT, 0));
        } else {
            return getCount(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                    key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId));
        }
    }

    public ImpulseCounterMeasurementItem getOlderUncalculatedImpulseCounterMeasurement(int channelId, long timestamp) {
        String[] projection = {
                SuplaContract.ImpulseCounterLogEntry._ID,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT
        };

        return getItem(ImpulseCounterMeasurementItem::new, projection, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP, timestamp),
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED, 0));
    }

    public Cursor getImpulseCounterMeasurements(int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
        return read(sqLiteDatabase -> {
            String sql = "SELECT SUM("
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER + ")" +
                    SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER + ", "
                    + " SUM("
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE + ")"
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE + ", "
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " FROM " + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME
                    + " WHERE "
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID
                    + " = " + channelId;

            if (dateFrom != null && dateTo != null) {
                sql += " AND "
                        + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                        + " >= " + toMilis(dateFrom)
                        + " AND "
                        + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                        + " <= " + toMilis(dateTo);
            }

            sql += " GROUP BY "
                    + " strftime('"
                    + groupByDateFormat
                    + "', " + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE + ")"
                    + " ORDER BY "
                    + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " ASC ";


            return sqLiteDatabase.rawQuery(sql, null);
        });
    }

    public void deleteImpulseCounterMeasurements(int channelId) {
        delete(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId));
    }

    public void deleteUncalculatedImpulseCounterMeasurements(int channelId) {
        delete(SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_INCREASE_CALCULATED, 0),
                key(SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID, channelId));
    }
}
