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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import dagger.hilt.android.EntryPointAccessors;
import java.util.Date;
import org.supla.android.data.source.DefaultMeasurableItemsRepository;
import org.supla.android.data.source.MeasurableItemsRepository;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.di.entrypoints.ProfileIdHolderEntryPoint;
import org.supla.android.profile.ProfileIdHolder;

public class MeasurementsDbHelper extends BaseDbHelper {

  public static final int DATABASE_VERSION = 37;
  public static final String DATABASE_NAME = "supla_measurements.db";
  private static final Object mutex = new Object();

  private static MeasurementsDbHelper instance;

  private final MeasurableItemsRepository measurableItemsRepository;

  private MeasurementsDbHelper(Context context, ProfileIdProvider profileIdProvider) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION, profileIdProvider);
    this.measurableItemsRepository =
        new DefaultMeasurableItemsRepository(new ThermostatLogDao(this));
  }

  /**
   * Gets a single instance of the {@link MeasurementsDbHelper} class. If the instance does not
   * exist, is created like in classic Singleton pattern.
   *
   * @param context The context.
   * @return {@link MeasurementsDbHelper} instance.
   */
  public static MeasurementsDbHelper getInstance(Context context) {
    MeasurementsDbHelper result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null) {
          ProfileIdHolder profileIdHolder =
              EntryPointAccessors.fromApplication(
                      context.getApplicationContext(), ProfileIdHolderEntryPoint.class)
                  .provideProfileIdHolder();
          instance =
              result = new MeasurementsDbHelper(context, () -> profileIdHolder.getProfileId());
        }
      }
    }
    return result;
  }

  @NonNull
  @Override
  public String getDatabaseNameForLog() {
    return DATABASE_NAME;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // Moved to Room (see LegacySchema.onCreate())
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Moved to Room (see DatabaseModule)
  }

  public Cursor getThermostatMeasurements(int channelId, Date dateFrom, Date dateTo) {
    return measurableItemsRepository.getThermostatMeasurements(channelId, dateFrom, dateTo);
  }

  public int getThermostatMeasurementTimestamp(int channelId, boolean min) {
    return measurableItemsRepository.getThermostatMeasurementTimestamp(channelId, min);
  }

  public int getThermostatMeasurementTotalCount(int channelId) {
    return measurableItemsRepository.getThermostatMeasurementTotalCount(channelId);
  }

  public void deleteThermostatMeasurements(int channelId) {
    measurableItemsRepository.deleteThermostatMeasurements(channelId);
  }

  public void addThermostatMeasurement(ThermostatMeasurementItem emi) {
    measurableItemsRepository.addThermostatMeasurement(emi);
  }
}
