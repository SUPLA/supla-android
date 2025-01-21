package org.supla.android.db.room.measurements.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
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

  @MockK
  private lateinit var dateProvider: DateProvider

  @InjectMockKs
  private lateinit var migration: MeasurementsDbMigration36to37

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should iterate over all tables`() {
    // given
    val db: SupportSQLiteDatabase = mockk(relaxed = true)
    every { dateProvider.currentTimestamp() } returnsMany listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)

    // when
    migration.migrate(db)

    // then
    verify(ordering = Ordering.ORDERED) {
      verifyPowerActiveLogTableCreation(db)
      verify(
        db,
        CurrentHistoryLogEntity.TABLE_NAME,
        CurrentHistoryLogEntity.COLUMN_GROUPING_STRING,
        CurrentHistoryLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        ElectricityMeterLogEntity.TABLE_NAME,
        ElectricityMeterLogEntity.COLUMN_GROUPING_STRING,
        ElectricityMeterLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        GeneralPurposeMeasurementEntity.TABLE_NAME,
        GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING,
        GeneralPurposeMeasurementEntity.COLUMN_DATE
      )
      verify(
        db,
        GeneralPurposeMeterEntity.TABLE_NAME,
        GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING,
        GeneralPurposeMeterEntity.COLUMN_DATE
      )
      verify(
        db,
        HomePlusThermostatLogEntity.TABLE_NAME,
        HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING,
        HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        ImpulseCounterLogEntity.TABLE_NAME,
        ImpulseCounterLogEntity.COLUMN_GROUPING_STRING,
        ImpulseCounterLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        TemperatureAndHumidityLogEntity.TABLE_NAME,
        TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING,
        TemperatureAndHumidityLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        TemperatureLogEntity.TABLE_NAME,
        TemperatureLogEntity.COLUMN_GROUPING_STRING,
        TemperatureLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        VoltageHistoryLogEntity.TABLE_NAME,
        VoltageHistoryLogEntity.COLUMN_GROUPING_STRING,
        VoltageHistoryLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        HumidityLogEntity.TABLE_NAME,
        HumidityLogEntity.COLUMN_GROUPING_STRING,
        HumidityLogEntity.COLUMN_DATE
      )
    }
    verify(exactly = 11) { dateProvider.currentTimestamp() }

    confirmVerified(db, dateProvider)
  }

  @Test
  fun `should stop migration if takes more then 10 seconds`() {
    // given
    val db: SupportSQLiteDatabase = mockk(relaxed = true)
    every { dateProvider.currentTimestamp() } returnsMany listOf(1L, 10_0003L)

    // when
    migration.migrate(db)

    // then
    verify(ordering = Ordering.ORDERED) {
      verifyPowerActiveLogTableCreation(db)
      verify(
        db,
        CurrentHistoryLogEntity.TABLE_NAME,
        CurrentHistoryLogEntity.COLUMN_GROUPING_STRING,
        CurrentHistoryLogEntity.COLUMN_TIMESTAMP
      )
      db.execSQL(alterString(ElectricityMeterLogEntity.TABLE_NAME, ElectricityMeterLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${ElectricityMeterLogEntity.TABLE_NAME}")
      db.execSQL(alterString(GeneralPurposeMeasurementEntity.TABLE_NAME, GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${GeneralPurposeMeasurementEntity.TABLE_NAME}")
      db.execSQL(alterString(GeneralPurposeMeterEntity.TABLE_NAME, GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${GeneralPurposeMeterEntity.TABLE_NAME}")
      db.execSQL(alterString(HomePlusThermostatLogEntity.TABLE_NAME, HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${HomePlusThermostatLogEntity.TABLE_NAME}")
      db.execSQL(alterString(ImpulseCounterLogEntity.TABLE_NAME, ImpulseCounterLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${ImpulseCounterLogEntity.TABLE_NAME}")
      db.execSQL(alterString(TemperatureAndHumidityLogEntity.TABLE_NAME, TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${TemperatureAndHumidityLogEntity.TABLE_NAME}")
      db.execSQL(alterString(TemperatureLogEntity.TABLE_NAME, TemperatureLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${TemperatureLogEntity.TABLE_NAME}")
      db.execSQL(alterString(VoltageHistoryLogEntity.TABLE_NAME, VoltageHistoryLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${VoltageHistoryLogEntity.TABLE_NAME}")
      db.execSQL(alterString(HumidityLogEntity.TABLE_NAME, HumidityLogEntity.COLUMN_GROUPING_STRING))
      db.execSQL("DELETE FROM ${HumidityLogEntity.TABLE_NAME}")
    }
    verify(exactly = 2) { dateProvider.currentTimestamp() }
    confirmVerified(db, dateProvider)
  }

  @Test
  fun `should delete data if could not migrate`() {
    // given
    val db: SupportSQLiteDatabase = mockk(relaxed = true)
    every {
      db.execSQL(
        updateString(
          ImpulseCounterLogEntity.TABLE_NAME,
          ImpulseCounterLogEntity.COLUMN_GROUPING_STRING,
          ImpulseCounterLogEntity.COLUMN_TIMESTAMP
        )
      )
    } throws Exception()
    every { dateProvider.currentTimestamp() } returnsMany listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)

    // when
    migration.migrate(db)

    // then
    verify(ordering = Ordering.ORDERED) {
      verifyPowerActiveLogTableCreation(db)
      verify(
        db,
        CurrentHistoryLogEntity.TABLE_NAME,
        CurrentHistoryLogEntity.COLUMN_GROUPING_STRING,
        CurrentHistoryLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        ElectricityMeterLogEntity.TABLE_NAME,
        ElectricityMeterLogEntity.COLUMN_GROUPING_STRING,
        ElectricityMeterLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        GeneralPurposeMeasurementEntity.TABLE_NAME,
        GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING,
        GeneralPurposeMeasurementEntity.COLUMN_DATE
      )
      verify(
        db,
        GeneralPurposeMeterEntity.TABLE_NAME,
        GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING,
        GeneralPurposeMeterEntity.COLUMN_DATE
      )
      verify(
        db,
        HomePlusThermostatLogEntity.TABLE_NAME,
        HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING,
        HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        ImpulseCounterLogEntity.TABLE_NAME,
        ImpulseCounterLogEntity.COLUMN_GROUPING_STRING,
        ImpulseCounterLogEntity.COLUMN_TIMESTAMP
      )
      db.execSQL("DELETE FROM ${ImpulseCounterLogEntity.TABLE_NAME}")
      verify(
        db,
        TemperatureAndHumidityLogEntity.TABLE_NAME,
        TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING,
        TemperatureAndHumidityLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        TemperatureLogEntity.TABLE_NAME,
        TemperatureLogEntity.COLUMN_GROUPING_STRING,
        TemperatureLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        VoltageHistoryLogEntity.TABLE_NAME,
        VoltageHistoryLogEntity.COLUMN_GROUPING_STRING,
        VoltageHistoryLogEntity.COLUMN_TIMESTAMP
      )
      verify(
        db,
        HumidityLogEntity.TABLE_NAME,
        HumidityLogEntity.COLUMN_GROUPING_STRING,
        HumidityLogEntity.COLUMN_DATE
      )
    }
    verify(exactly = 11) { dateProvider.currentTimestamp() }

    confirmVerified(db, dateProvider)
  }

  private fun verifyPowerActiveLogTableCreation(db: SupportSQLiteDatabase) {
    db.execSQL(PowerActiveHistoryLogEntity.SQL[0])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[1])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[2])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[3])
    db.execSQL(PowerActiveHistoryLogEntity.SQL[4])
  }

  private fun verify(db: SupportSQLiteDatabase, tableName: String, groupingColumn: String, dateColumn: String) {
    db.execSQL(alterString(tableName, groupingColumn))
    db.execSQL(updateString(tableName, groupingColumn, dateColumn))
  }

  private fun alterString(tableName: String, columnName: String): String =
    "ALTER TABLE $tableName ADD COLUMN $columnName TEXT NOT NULL DEFAULT ''"

  private fun updateString(tableName: String, groupingColumn: String, dateColumn: String): String =
    """
      UPDATE $tableName 
      SET $groupingColumn = 
        STRFTIME('%Y%m%d%H%M', DATETIME($dateColumn/1000, 'unixepoch')) || CASE (STRFTIME('%w', DATETIME($dateColumn/1000, 'unixepoch'))) 
          WHEN '1' THEN '1' 
          WHEN '2' THEN '2' 
          WHEN '3' THEN '3' 
          WHEN '4' THEN '4' 
          WHEN '5' THEN '5' 
          WHEN '6' THEN '6' 
          ELSE '7' 
        END
    """.trimIndent()
}
