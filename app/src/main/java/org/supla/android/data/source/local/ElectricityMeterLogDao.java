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

import org.supla.android.db.ElectricityMeasurementItem;
import org.supla.android.db.SuplaContract;

import java.util.Date;

public class ElectricityMeterLogDao extends MeasurementsBaseDao {
    public ElectricityMeterLogDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    public void addElectricityMeasurement(ElectricityMeasurementItem emi) {
        insert(emi, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public double getLastElectricityMeterMeasurementValue(int monthOffset, int channelId, boolean production) {
        String colPhase1;
        String colPhase2;
        String colPhase3;

        if (production) {
            colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE;
            colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE;
            colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE;
        } else {
            colPhase1 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE;
            colPhase2 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE;
            colPhase3 = SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE;
        }

        return getLastMeasurementValue(SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME,
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID,
                "IFNULL("
                        + colPhase1
                        + ", 0) + IFNULL("
                        + colPhase2
                        + ",0) + IFNULL("
                        + colPhase3
                        + ",0)",
                monthOffset, channelId);
    }

    public int getElectricityMeterMeasurementTimestamp(int channelId, boolean min) {
        return getMeasurementTimestamp(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId, min);

    }

    public int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement) {
        if (withoutComplement) {
            return getCount(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                    key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId),
                    key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT, 0));
        } else {

            return getCount(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                    key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId)
            );
        }
    }

    public ElectricityMeasurementItem getOlderUncalculatedElectricityMeasurement(int channelId, long timestamp) {
        String[] projection = {
                SuplaContract.ElectricityMeterLogEntry._ID,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT
        };

        return getItem(ElectricityMeasurementItem::new, projection, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId),
                key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP, timestamp),
                key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED, 0));
    }

    public Cursor getElectricityMeasurementsCursor(int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
        return read(sqLiteDatabase -> {
            String sql = "SELECT SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE + ", "
                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE + ", "
                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE + ", "

                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE + ", "
                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE + ", "
                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE + ", "

                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED + ", "
                    + " SUM(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED + ", "

                    + " MAX(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ", "
                    + " MAX(" + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + ")" +
                    SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " FROM " + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME
                    + " WHERE "
                    + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID
                    + " = " + channelId;

            if (dateFrom != null && dateTo != null) {
                sql += " AND "
                        + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + " >= " + toMilis(dateFrom)
                        + " AND "
                        + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + " <= " + toMilis(dateTo);
            }

            sql += " GROUP BY "
                    + " strftime('"
                    + groupByDateFormat
                    + "', " + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ")"
                    + " ORDER BY "
                    + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP
                    + " ASC ";

            return sqLiteDatabase.rawQuery(sql, null);
        });
    }

    public void deleteElectricityMeasurements(int channelId) {
        delete(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME, key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId));
    }

    public void deleteUncalculatedElectricityMeasurements(int channelId) {
        delete(SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_INCREASE_CALCULATED, 0),
                key(SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID, channelId));
    }
}
