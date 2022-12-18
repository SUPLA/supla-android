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

import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.data.source.DefaultMeasurableItemsRepository;
import org.supla.android.data.source.MeasurableItemsRepository;
import org.supla.android.data.source.local.ElectricityMeterLogDao;
import org.supla.android.data.source.local.ImpulseCounterLogDao;
import org.supla.android.data.source.local.TempHumidityLogDao;
import org.supla.android.data.source.local.TemperatureLogDao;
import org.supla.android.data.source.local.ThermostatLogDao;
import org.supla.android.di.ProfileIdHolderEntryPoint;
import org.supla.android.profile.ProfileIdHolder;

import java.util.Date;

import static org.supla.android.data.source.local.BaseDao.timestampStartsWithTheCurrentMonth;
import static org.supla.android.db.DbHelper.DATABASE_VERSION;

import dagger.hilt.android.EntryPointAccessors;

public class MeasurementsDbHelper extends BaseDbHelper {

    private static final String M_DATABASE_NAME = "supla_measurements.db";
    private static final Object mutex = new Object();

    private static MeasurementsDbHelper instance;

    private final MeasurableItemsRepository measurableItemsRepository;

    private MeasurementsDbHelper(Context context, ProfileIdProvider profileIdProvider) {
        super(context, M_DATABASE_NAME, null, DATABASE_VERSION, profileIdProvider);
        this.measurableItemsRepository = new DefaultMeasurableItemsRepository(
                new ImpulseCounterLogDao(this),
                new ElectricityMeterLogDao(this),
                new ThermostatLogDao(this),
                new TempHumidityLogDao(this),
                new TemperatureLogDao(this));
    }

    /**
     * Gets a single instance of the {@link MeasurementsDbHelper} class. If the instance does not exist, is created like in classic Singleton pattern.
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
                    ProfileIdHolder profileIdHolder = EntryPointAccessors.fromApplication(
                            context.getApplicationContext(), ProfileIdHolderEntryPoint.class)
                            .provideProfileIdHolder();
                    instance = result = new MeasurementsDbHelper(context, () -> profileIdHolder.getProfileId());
                }
            }
        }
        return result;
    }

    @Override
    protected String getDatabaseNameForLog() {
        return M_DATABASE_NAME;
    }

    private void createImpulseCounterLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_ICLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + " (" +
                SuplaContract.ImpulseCounterLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER + " BIGINT NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE
                + " DOUBLE NOT NULL," +
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT
                + " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_ICLOG_TABLE);
        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP);

        createIndex(db, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
                SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createImpulseCounterLogView(SQLiteDatabase db) {

        final String SQL_CREATE_EM_VIEW = "CREATE VIEW "
                + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME + " AS "
                + "SELECT " + SuplaContract.ImpulseCounterLogEntry._ID + " "
                + SuplaContract.ImpulseCounterLogViewEntry._ID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP + ", "
                + "datetime(" + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP
                + ", 'unixepoch', 'localtime') "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COMPLEMENT + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID + " "
                + SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_PROFILEID
                + " FROM " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME;

        execSQL(db, SQL_CREATE_EM_VIEW);
    }

    private void createElectricityMeterLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_EMLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + " (" +
                SuplaContract.ElectricityMeterLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED + " REAL NULL," +
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT +
                " INTEGER NOT NULL)";

        execSQL(db, SQL_CREATE_EMLOG_TABLE);
        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP);

        createIndex(db, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
                SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + " )";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createElectricityMeterLogView(SQLiteDatabase db) {

        final String SQL_CREATE_EM_VIEW = "CREATE VIEW "
                + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME + " AS "
                + "SELECT " + SuplaContract.ElectricityMeterLogEntry._ID + " "
                + SuplaContract.ElectricityMeterLogViewEntry._ID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP + ", "
                + "datetime(" + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP
                + ", 'unixepoch', 'localtime') "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE + ", "

                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE + ", "

                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED + ", "

                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_COMPLEMENT + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PROFILEID + " "
                + SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PROFILEID
                + " FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME;

        execSQL(db, SQL_CREATE_EM_VIEW);
    }

    private void createThermostatLogTable(SQLiteDatabase db) {

        final String SQL_CREATE_THLOG_TABLE = "CREATE TABLE " +
                SuplaContract.ThermostatLogEntry.TABLE_NAME + " (" +
                SuplaContract.ThermostatLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_ON + " INTEGER NOT NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE
                + " DECIMAL(5,2) NULL," +
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE
                + " DECIMAL(5,2) NULL)";

        execSQL(db, SQL_CREATE_THLOG_TABLE);
        createIndex(db, SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.ThermostatLogEntry.TABLE_NAME,
                SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME
                + "(" + SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createTempHumidityLogTable(SQLiteDatabase db) {
        final String SQL_CREATE_THLOG_TABLE = "CREATE TABLE " +
                SuplaContract.TempHumidityLogEntry.TABLE_NAME + " (" +
                SuplaContract.TempHumidityLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TEMPERATURE
                + " DECIMAL(8,4) NULL," +
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_HUMIDITY
                + " DECIMAL(8,4) NULL)";

        execSQL(db, SQL_CREATE_THLOG_TABLE);
        createIndex(db, SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.TempHumidityLogEntry.TABLE_NAME,
                SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME
                + "(" + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    private void createTemperatureLogTable(SQLiteDatabase db) {
        final String SQL_CREATE_TLOG_TABLE = "CREATE TABLE " +
                SuplaContract.TemperatureLogEntry.TABLE_NAME + " (" +
                SuplaContract.TemperatureLogEntry._ID + " INTEGER PRIMARY KEY," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID + " INTEGER NOT NULL," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP + " BIGINT NOT NULL," +
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TEMPERATURE
                + " DECIMAL(8,4) NULL)";

        execSQL(db, SQL_CREATE_TLOG_TABLE);
        createIndex(db, SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID);

        createIndex(db, SuplaContract.TemperatureLogEntry.TABLE_NAME,
                SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP);

        final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME
                + "(" + SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP + ")";

        execSQL(db, SQL_CREATE_INDEX);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createImpulseCounterLogTable(db);
        createElectricityMeterLogTable(db);
        createThermostatLogTable(db);
        createTempHumidityLogTable(db);
        createTemperatureLogTable(db);
        upgradeToV22(db);
        upgradeToV24(db);

        // Create views at the end
        createImpulseCounterLogView(db);
        createElectricityMeterLogView(db);
    }

    private void upgradeToV6(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV6");
        createElectricityMeterLogTable(db);
    }

    private void upgradeToV7(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV7");
        createThermostatLogTable(db);
    }

    private void upgradeToV9(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV9");
        createImpulseCounterLogTable(db);
        createTempHumidityLogTable(db);
        createTemperatureLogTable(db);
    }

    private void upgradeToV10(SQLiteDatabase db) {
        Trace.d(DbHelper.class.getName(), "upgradeToV10");
        execSQL(db, "DROP TABLE " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
        createImpulseCounterLogTable(db);
    }

    private void recreateTables(SQLiteDatabase db) {
        execSQL(db, "DROP TABLE " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
        execSQL(db, "DROP TABLE " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);

        createImpulseCounterLogTable(db);
        createElectricityMeterLogTable(db);
    }

    private void upgradeToV11(SQLiteDatabase db) {
        recreateTables(db);
    }

    private void upgradeToV12(SQLiteDatabase db) {
        execSQL(db, "DELETE FROM " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
        execSQL(db, "DELETE FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);
    }

    private void upgradeToV14(SQLiteDatabase db) {
        addColumn(db, "ALTER TABLE "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED
                + " REAL NULL");

        addColumn(db, "ALTER TABLE "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + " ADD COLUMN " + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED
                + " REAL NULL");
    }

    private void upgradeToV18(SQLiteDatabase db) {
        recreateTables(db);
    }

    private void upgradeToV22(SQLiteDatabase db) {
        String column_name = "profileid";
        String tables[] = {
            SuplaContract.ElectricityMeterLogEntry.TABLE_NAME,
            SuplaContract.ImpulseCounterLogEntry.TABLE_NAME,
            SuplaContract.ThermostatLogEntry.TABLE_NAME,
            SuplaContract.TemperatureLogEntry.TABLE_NAME,
            SuplaContract.TempHumidityLogEntry.TABLE_NAME
        };
            
        for(String table: tables) {
            addColumn(db, "ALTER TABLE " + table +
                      " ADD COLUMN " + column_name + " INTEGER NOT NULL DEFAULT 1");                  
        }

    }

    private void upgradeToV24(SQLiteDatabase db) {
        // Clear data and extend unique indexes with profile id.
        String unique = "_unique_index";

        execSQL(db, "DROP INDEX " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + unique);
        execSQL(db, "DELETE FROM " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME);
        execSQL(db, "CREATE UNIQUE INDEX "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME + unique + " ON "
                + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP + ", "
                + SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PROFILEID + " )");

        execSQL(db, "DROP INDEX " + SuplaContract.ThermostatLogEntry.TABLE_NAME + unique);
        execSQL(db, "DELETE FROM " + SuplaContract.ThermostatLogEntry.TABLE_NAME);
        execSQL(db, "CREATE UNIQUE INDEX "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.ThermostatLogEntry.TABLE_NAME
                + "(" + SuplaContract.ThermostatLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ThermostatLogEntry.COLUMN_NAME_TIMESTAMP + ")");
        
        execSQL(db, "DROP INDEX " + SuplaContract.TemperatureLogEntry.TABLE_NAME + unique);
        execSQL(db, "DELETE FROM " + SuplaContract.TemperatureLogEntry.TABLE_NAME);
        execSQL(db, "CREATE UNIQUE INDEX "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TemperatureLogEntry.TABLE_NAME
                + "(" + SuplaContract.TemperatureLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TemperatureLogEntry.COLUMN_NAME_TIMESTAMP + ")");

        execSQL(db, "DROP INDEX " + SuplaContract.TempHumidityLogEntry.TABLE_NAME + unique);
        execSQL(db, "DELETE FROM " + SuplaContract.TempHumidityLogEntry.TABLE_NAME);
        execSQL(db, "CREATE UNIQUE INDEX "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME + "_unique_index ON "
                + SuplaContract.TempHumidityLogEntry.TABLE_NAME
                + "(" + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.TempHumidityLogEntry.COLUMN_NAME_TIMESTAMP + ")");
       
        
        execSQL(db, "DROP INDEX " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + unique);
        execSQL(db, "DELETE FROM " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME);
        execSQL(db, "CREATE UNIQUE INDEX "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME + unique + " ON "
                + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME
                + "(" + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP + ", "
                + SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID + ")");
        
    }


    private void recreateViews(SQLiteDatabase db) {
        execSQL(db, "DROP VIEW IF EXISTS " + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME);
        execSQL(db, "DROP VIEW IF EXISTS " + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME);

        createImpulseCounterLogView(db);
        createElectricityMeterLogView(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < newVersion) {
            for (int nv = oldVersion; nv < newVersion; nv++) {
                switch (nv) {
                    case 5:
                        upgradeToV6(db);
                        break;
                    case 6:
                        upgradeToV7(db);
                        break;
                    case 8:
                        upgradeToV9(db);
                        break;
                    case 9:
                        upgradeToV10(db);
                        break;
                    case 10:
                        upgradeToV11(db);
                        break;
                    case 11:
                        upgradeToV12(db);
                        break;
                    case 13:
                        upgradeToV14(db);
                        break;
                    case 17:
                        upgradeToV18(db);
                        break;
                    case 21:
                        upgradeToV22(db);
                        break;

                    case 23:
                        upgradeToV24(db);
                        break;
                }
            }

            // Recreate views on the end
            recreateViews(db);
        }

    }

    public boolean electricityMeterMeasurementsStartsWithTheCurrentMonth(int channelId) {
        long minTS = getElectricityMeterMeasurementTimestamp(channelId, true);
        return timestampStartsWithTheCurrentMonth(minTS);
    }

    public int getElectricityMeterMeasurementTimestamp(int channelId, boolean min) {
        return measurableItemsRepository.getElectricityMeterMeasurementTimestamp(channelId, min);
    }

    public double getLastElectricityMeterMeasurementValue(int monthOffset, int channelId, boolean production) {
        return measurableItemsRepository.getLastElectricityMeterMeasurementValue(monthOffset, channelId, production);
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

    public Cursor getElectricityMeasurements(int channelId, String groupByDateFormat, Date dateFrom, Date dateTo) {
        return measurableItemsRepository.getElectricityMeasurementsCursor(channelId, groupByDateFormat, dateFrom, dateTo);
    }

    public int getElectricityMeterMeasurementTotalCount(int channelId, boolean withoutComplement) {
        return measurableItemsRepository.getElectricityMeterMeasurementTotalCount(channelId, withoutComplement);
    }

    public void addElectricityMeasurement(ElectricityMeasurementItem emi) {
        measurableItemsRepository.addElectricityMeasurement(emi);
    }

    public void deleteElectricityMeasurements(int channelId) {
        measurableItemsRepository.deleteElectricityMeasurements(channelId);
    }

    public Cursor getThermostatMeasurements(int channelId) {
        return measurableItemsRepository.getThermostatMeasurements(channelId);
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

    public Cursor getTempHumidityMeasurements(int channelId, Date dateFrom, Date dateTo) {
        return measurableItemsRepository.getTempHumidityMeasurements(channelId, dateFrom, dateTo);
    }

    public int getTempHumidityMeasurementTimestamp(int channelId, boolean min) {
        return measurableItemsRepository.getTempHumidityMeasurementTimestamp(channelId, min);
    }

    public int getTempHumidityMeasurementTotalCount(int channelId) {
        return measurableItemsRepository.getTempHumidityMeasurementTotalCount(channelId);
    }

    public void deleteTempHumidityMeasurements(int channelId) {
        measurableItemsRepository.deleteTempHumidityMeasurements(channelId);
    }

    public void addTempHumidityMeasurement(TempHumidityMeasurementItem emi) {
        measurableItemsRepository.addTempHumidityMeasurement(emi);
    }

    public Cursor getTemperatureMeasurements(int channelId, Date dateFrom, Date dateTo) {
        return measurableItemsRepository.getTemperatureMeasurements(channelId, dateFrom, dateTo);
    }

    public int getTemperatureMeasurementTimestamp(int channelId, boolean min) {
        return measurableItemsRepository.getTemperatureMeasurementTimestamp(channelId, min);
    }

    public int getTemperatureMeasurementTotalCount(int channelId) {
        return measurableItemsRepository.getTemperatureMeasurementTotalCount(channelId);
    }

    public void deleteTemperatureMeasurements(int channelId) {
        measurableItemsRepository.deleteTemperatureMeasurements(channelId);
    }

    public void addTemperatureMeasurement(TemperatureMeasurementItem emi) {
        measurableItemsRepository.addTemperatureMeasurement(emi);
    }

    public Cursor getImpulseCounterMeasurements(int channelId, String groupByDateFormat,
                                                Date dateFrom, Date dateTo) {
        return measurableItemsRepository.getImpulseCounterMeasurements(channelId, groupByDateFormat, dateFrom, dateTo);
    }

    public void addImpulseCounterMeasurement(ImpulseCounterMeasurementItem item) {
        measurableItemsRepository.addImpulseCounterMeasurement(item);
    }

    public int getImpulseCounterMeasurementTotalCount(int channelId, boolean withoutComplement) {
        return measurableItemsRepository.getImpulseCounterMeasurementTotalCount(channelId, withoutComplement);
    }

    public void deleteImpulseCounterMeasurements(int channelId) {
        measurableItemsRepository.deleteImpulseCounterMeasurements(channelId);
    }
}
