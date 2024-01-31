package org.supla.android.db.room.measurements.migrations
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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.SuplaContract
import org.supla.android.db.room.SqlExecutor
import org.supla.android.db.room.measurements.MeasurementsLegacySchema
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsDbMigration29to30 @Inject constructor(
  private val legacySchema: MeasurementsLegacySchema
) : Migration(29, 30), SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  override fun migrate(database: SupportSQLiteDatabase) {
    execSQL(database, "DROP VIEW IF EXISTS " + SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME)
    execSQL(database, "DROP VIEW IF EXISTS " + SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME)

    execSQL(database, "DROP TABLE IF EXISTS " + SuplaContract.ImpulseCounterLogEntry.TABLE_NAME)
    execSQL(database, "DROP TABLE IF EXISTS " + SuplaContract.ElectricityMeterLogEntry.TABLE_NAME)
    execSQL(database, "DROP TABLE IF EXISTS " + TemperatureLogEntity.TABLE_NAME)
    execSQL(database, "DROP TABLE IF EXISTS " + SuplaContract.ThermostatLogEntry.TABLE_NAME)
    execSQL(database, "DROP TABLE IF EXISTS " + TemperatureAndHumidityLogEntity.TABLE_NAME)

    legacySchema.onCreate(database)

    execSQL(database, TemperatureLogEntity.SQL)
    execSQL(database, TemperatureAndHumidityLogEntity.SQL)
  }
}
