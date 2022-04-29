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
import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

public abstract class MeasurementsBaseDao extends BaseDao {

    MeasurementsBaseDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        super(databaseAccessProvider);
    }

    double getLastMeasurementValue(String tableName, String colTimestamp,
                                   String colChannelId, String colValue, int monthOffset,
                                   int channelId) {
        String[] projection = {
                "SUM(" + colValue + ")"
        };

        String selection = colChannelId
                + " = ? AND " + colTimestamp + " <= ?" +
            " AND profileid = ? ";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(lastSecondInMonthWithOffset(monthOffset).getTimeInMillis() / 1000),
                String.valueOf(getCachedProfileId())
        };

        return read(sqLiteDatabase -> {
            Cursor c = sqLiteDatabase.query(
                    tableName,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    colTimestamp + " DESC",
                    "1");

            double result = c.getCount();
            if (c.moveToFirst()) {
                result = c.getDouble(0);
            }
            c.close();

            return result;
        });
    }

    int getMeasurementTimestamp(String tableName, String colTimestamp, String colChannelId,
                                int channelId, boolean min) {

        String selection = "SELECT " +
                (min ? "MIN" : "MAX")
                + "("
                + colTimestamp + ") FROM "
                + tableName
                + " WHERE " + colChannelId
                + " = " + channelId
                + " AND profileid = " + getCachedProfileId();

        return read(sqLiteDatabase -> {
            Cursor c = sqLiteDatabase.rawQuery(selection, null);
            c.moveToFirst();
            int max = c.getInt(0);
            c.close();
            return max;
        });
    }

    long toMilis(Date date) {
        return date.getTime() / 1000;
    }

    private Calendar lastSecondInMonthWithOffset(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.add(Calendar.SECOND, -1);
        return calendar;
    }
}
