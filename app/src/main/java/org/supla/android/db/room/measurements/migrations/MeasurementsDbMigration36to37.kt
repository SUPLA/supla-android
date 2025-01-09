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
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import org.supla.android.db.room.SqlExecutor
import org.supla.android.db.room.measurements.MeasurementsDatabase
import javax.inject.Singleton

@Singleton
object MeasurementsDbMigration36to37 : Migration(36, 37), SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDatabase.NAME

  private val tables: List<Pair<String, String>> = listOf(
    Pair(CurrentHistoryLogEntity.TABLE_NAME, CurrentHistoryLogEntity.COLUMN_GROUPING_STRING),
    Pair(ElectricityMeterLogEntity.TABLE_NAME, ElectricityMeterLogEntity.COLUMN_GROUPING_STRING),
    Pair(GeneralPurposeMeasurementEntity.TABLE_NAME, GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING),
    Pair(GeneralPurposeMeterEntity.TABLE_NAME, GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING),
    Pair(HomePlusThermostatLogEntity.TABLE_NAME, HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING),
    Pair(ImpulseCounterLogEntity.TABLE_NAME, ImpulseCounterLogEntity.COLUMN_GROUPING_STRING),
    Pair(TemperatureAndHumidityLogEntity.TABLE_NAME, TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING),
    Pair(TemperatureLogEntity.TABLE_NAME, TemperatureLogEntity.COLUMN_GROUPING_STRING),
    Pair(VoltageHistoryLogEntity.TABLE_NAME, VoltageHistoryLogEntity.COLUMN_GROUPING_STRING),
    Pair(HumidityLogEntity.TABLE_NAME, HumidityLogEntity.COLUMN_GROUPING_STRING)
  )

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, PowerActiveHistoryLogEntity.SQL)

    tables.forEach { (table, groupingColumn) ->
      execSQL(db, alterString(table, groupingColumn))
    }
  }

  private fun alterString(tableName: String, columnName: String): String =
    "ALTER TABLE $tableName ADD COLUMN $columnName TEXT NOT NULL DEFAULT ''"
}
