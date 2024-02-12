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

import static org.supla.android.data.source.local.BaseDao.timestampStartsWithTheCurrentMonth;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import dagger.hilt.android.EntryPointAccessors;
import java.util.Date;
import org.supla.android.data.source.DefaultMeasurableItemsRepository;
import org.supla.android.data.source.MeasurableItemsRepository;
import org.supla.android.data.source.local.ElectricityMeterLogDao;
import org.supla.android.data.source.local.ImpulseCounterLogDao;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.di.entrypoints.ProfileIdHolderEntryPoint;
import org.supla.android.profile.ProfileIdHolder;

public class MeasurementsDbHelper extends BaseDbHelper {

  public static final int DATABASE_VERSION = 33;
  public static final String DATABASE_NAME = "supla_measurements.db";
  private static final Object mutex = new Object();

  private static MeasurementsDbHelper instance;

  private final MeasurableItemsRepository measurableItemsRepository;

  private MeasurementsDbHelper(Context context, ProfileIdProvider profileIdProvider) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION, profileIdProvider);
    this.measurableItemsRepository =
        new DefaultMeasurableItemsRepository(
            new ImpulseCounterLogDao(this),
            new ElectricityMeterLogDao(this),
            new ThermostatLogDao(this));
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

  public boolean electricityMeterMeasurementsStartsWithTheCurrentMonth(int channelId) {
    long minTS = getElectricityMeterMeasurementTimestamp(channelId, true);
    return timestampStartsWithTheCurrentMonth(minTS);
  }

  public int getElectricityMeterMeasurementTimestamp(int channelId, boolean min) {
    return measurableItemsRepository.getElectricityMeterMeasurementTimestamp(channelId, min);
  }

  public double getLastElectricityMeterMeasurementValue(
      int monthOffset, int channelId, boolean production) {
    return measurableItemsRepository.getLastElectricityMeterMeasurementValue(
        monthOffset, channelId, production);
  }

  public boolean impulseCounterMeasurementsStartsWithTheCurrentMonth(int channelId) {
    return measurableItemsRepository.impulseCounterMeasurementsStartsWithTheCurrentMonth(channelId);
  }

  public int getImpulseCounterMeasurementTimestamp(int channelId, boolean min) {
    return measurableItemsRepository.getImpulseCounterMeasurementTimestamp(channelId, min);
  }

  public double getLastImpulseCounterMeasurementValue(int monthOffset, int channelId) {
    return measurableItemsRepository.getLastImpulseCounterMeasurementValue(monthOffset, channelId);
  }

  public Cursor getElectricityMeasurements(
      int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
    return measurableItemsRepository.getElectricityMeasurementsCursor(
        channelId, groupByDateFormat, dateFrom, dateTo);
  }

  public int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement) {
    return measurableItemsRepository.getElectricityMeterMeasurementTotalCount(
        channelId, withoutComplement);
  }

  public void addElectricityMeasurement(ElectricityMeasurementItem emi) {
    measurableItemsRepository.addElectricityMeasurement(emi);
  }

  public void deleteElectricityMeasurements(int channelId) {
    measurableItemsRepository.deleteElectricityMeasurements(channelId);
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

  public Cursor getImpulseCounterMeasurements(
      int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
    return measurableItemsRepository.getImpulseCounterMeasurements(
        channelId, groupByDateFormat, dateFrom, dateTo);
  }

  public void addImpulseCounterMeasurement(ImpulseCounterMeasurementItem item) {
    measurableItemsRepository.addImpulseCounterMeasurement(item);
  }

  public int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement) {
    return measurableItemsRepository.getImpulseCounterMeasurementTotalCount(
        channelId, withoutComplement);
  }

  public void deleteImpulseCounterMeasurements(int channelId) {
    measurableItemsRepository.deleteImpulseCounterMeasurements(channelId);
  }
}
