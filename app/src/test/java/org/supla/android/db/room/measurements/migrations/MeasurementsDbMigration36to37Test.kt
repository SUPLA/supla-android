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

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.Ordering
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
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

class MeasurementsDbMigration36to37Test {

  @Test
  fun `should iterate over all tables`() {
    // given
    val db: SupportSQLiteDatabase = mockk(relaxed = true)

    // when
    MeasurementsDbMigration36to37.migrate(db)

    // then
    verify(ordering = Ordering.ORDERED) {
      verifyPowerActiveLogTableCreation(db)

      verify(db, CurrentHistoryLogEntity.TABLE_NAME, CurrentHistoryLogEntity.COLUMN_GROUPING_STRING)
      verify(db, ElectricityMeterLogEntity.TABLE_NAME, ElectricityMeterLogEntity.COLUMN_GROUPING_STRING)
      verify(db, GeneralPurposeMeasurementEntity.TABLE_NAME, GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING)
      verify(db, GeneralPurposeMeterEntity.TABLE_NAME, GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING)
      verify(db, HomePlusThermostatLogEntity.TABLE_NAME, HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING)
      verify(db, ImpulseCounterLogEntity.TABLE_NAME, ImpulseCounterLogEntity.COLUMN_GROUPING_STRING)
      verify(db, TemperatureAndHumidityLogEntity.TABLE_NAME, TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING)
      verify(db, TemperatureLogEntity.TABLE_NAME, TemperatureLogEntity.COLUMN_GROUPING_STRING)
      verify(db, VoltageHistoryLogEntity.TABLE_NAME, VoltageHistoryLogEntity.COLUMN_GROUPING_STRING)
      verify(db, HumidityLogEntity.TABLE_NAME, HumidityLogEntity.COLUMN_GROUPING_STRING)
    }

    confirmVerified(db)
  }

  private fun verifyPowerActiveLogTableCreation(db: SupportSQLiteDatabase) {
    db.execSQL(PowerActiveHistoryLogEntity.SQL[0])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[1])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[2])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[3])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[4])
  }

  private fun verify(db: SupportSQLiteDatabase, tableName: String, groupingColumn: String) {
    db.execSQL(alterString(tableName, groupingColumn))
  }

  private fun alterString(tableName: String, columnName: String): String =
    "ALTER TABLE $tableName ADD COLUMN $columnName TEXT NOT NULL DEFAULT ''"
}
