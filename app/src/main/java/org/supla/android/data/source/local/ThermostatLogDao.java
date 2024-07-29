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
import java.util.Date;
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity;
import org.supla.android.db.ThermostatMeasurementItem;

public class ThermostatLogDao extends MeasurementsBaseDao {

  public ThermostatLogDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    super(databaseAccessProvider);
  }

  public int getThermostatMeasurementTimestamp(int channelId, boolean min) {
    return getMeasurementTimestamp(
        HomePlusThermostatLogEntity.TABLE_NAME,
        HomePlusThermostatLogEntity.COLUMN_TIMESTAMP,
        HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID,
        channelId,
        min);
  }

  public int getThermostatMeasurementTotalCount(int channelId) {
    return getCount(
        HomePlusThermostatLogEntity.TABLE_NAME,
        key(HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID, channelId),
        key(HomePlusThermostatLogEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public void deleteThermostatMeasurements(int channelId) {
    delete(
        HomePlusThermostatLogEntity.TABLE_NAME,
        key(HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID, channelId),
        key(HomePlusThermostatLogEntity.COLUMN_PROFILE_ID, getCachedProfileId()));
  }

  public void addThermostatMeasurement(ThermostatMeasurementItem emi) {
    insert(emi, HomePlusThermostatLogEntity.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE);
  }

  public Cursor getThermostatMeasurements(int channelId, Date dateFrom, Date dateTo) {
    String dates = "";
    if (dateFrom != null && dateTo != null) {
      dates =
          " AND "
              + HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
              + " >= "
              + toMilis(dateFrom)
              + " AND "
              + HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
              + " <= "
              + toMilis(dateTo);
    }
    String sql =
        "SELECT "
            + HomePlusThermostatLogEntity.COLUMN_MEASURED_TEMPERATURE
            + ", "
            + HomePlusThermostatLogEntity.COLUMN_PRESET_TEMPERATURE
            + ", "
            + HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
            + " FROM "
            + HomePlusThermostatLogEntity.TABLE_NAME
            + " WHERE "
            + HomePlusThermostatLogEntity.COLUMN_CHANNEL_ID
            + " = "
            + channelId
            + dates
            + " ORDER BY "
            + HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
            + " ASC ";

    return read(sqLiteDatabase -> sqLiteDatabase.rawQuery(sql, null));
  }
}
