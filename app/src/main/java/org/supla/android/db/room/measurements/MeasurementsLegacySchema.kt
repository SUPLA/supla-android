package org.supla.android.db.room.measurements
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
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.SuplaContract
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry
import org.supla.android.db.SuplaContract.ImpulseCounterLogEntry
import org.supla.android.db.SuplaContract.ThermostatLogEntry
import org.supla.android.db.room.SqlExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsLegacySchema @Inject constructor() : SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  fun onCreate(db: SupportSQLiteDatabase) {
    createImpulseCounterLogTable(db)
    createElectricityMeterLogTable(db)
    createThermostatLogTable(db)

    // Create views at the end
    createImpulseCounterLogView(db)
    createElectricityMeterLogView(db)
  }

  private fun createImpulseCounterLogTable(db: SupportSQLiteDatabase) {
    val sqlCreateIclogTable = """
      CREATE TABLE ${ImpulseCounterLogEntry.TABLE_NAME} 
      (
        ${ImpulseCounterLogEntry._ID} INTEGER PRIMARY KEY,
        ${ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID} INTEGER NOT NULL,
        ${ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP} INTEGER NOT NULL,
        ${ImpulseCounterLogEntry.COLUMN_NAME_COUNTER} INTEGER NOT NULL,
        ${ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE} REAL NOT NULL,
        ${ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT} INTEGER NOT NULL,
        ${ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID} INTEGER NOT NULL
      )
    """.trimIndent()

    execSQL(db, sqlCreateIclogTable)
    createIndex(db, ImpulseCounterLogEntry.TABLE_NAME, ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID)
    createIndex(db, ImpulseCounterLogEntry.TABLE_NAME, ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP)
    createIndex(db, ImpulseCounterLogEntry.TABLE_NAME, ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT)
    val sqlCreateIndex = """
      CREATE UNIQUE INDEX ${ImpulseCounterLogEntry.TABLE_NAME}_unique_index ON ${ImpulseCounterLogEntry.TABLE_NAME}
      (
        ${ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID},
        ${ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP},
        ${ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID}
      )
    """.trimIndent()
    execSQL(db, sqlCreateIndex)
  }

  private fun createImpulseCounterLogView(db: SupportSQLiteDatabase) {
    val sqlCreateIcView = """
      CREATE VIEW ${SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME} AS
        SELECT 
          ${ImpulseCounterLogEntry._ID} ${SuplaContract.ImpulseCounterLogViewEntry._ID},
          ${ImpulseCounterLogEntry.COLUMN_NAME_CHANNELID} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID},
          ${ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP},
          datetime(${ImpulseCounterLogEntry.COLUMN_NAME_TIMESTAMP}, 'unixepoch', 'localtime') 
            ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE},
          ${ImpulseCounterLogEntry.COLUMN_NAME_COUNTER} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER},
          ${ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE},
          ${ImpulseCounterLogEntry.COLUMN_NAME_COMPLEMENT} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COMPLEMENT},
          ${ImpulseCounterLogEntry.COLUMN_NAME_PROFILEID} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_PROFILEID}
         FROM ${ImpulseCounterLogEntry.TABLE_NAME}
    """.trimIndent()

    execSQL(db, sqlCreateIcView)
  }

  private fun createElectricityMeterLogTable(db: SupportSQLiteDatabase) {
    val sqlCreateEmlogTable = """
      CREATE TABLE ${ElectricityMeterLogEntry.TABLE_NAME}
      (
        ${ElectricityMeterLogEntry._ID} INTEGER PRIMARY KEY,
        ${ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID} INTEGER NOT NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP} INTEGER NOT NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED} REAL NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT} INTEGER NOT NULL,
        ${ElectricityMeterLogEntry.COLUMN_NAME_PROFILEID} INTEGER NOT NULL
      )
    """.trimIndent()
    execSQL(db, sqlCreateEmlogTable)
    createIndex(db, ElectricityMeterLogEntry.TABLE_NAME, ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID)
    createIndex(db, ElectricityMeterLogEntry.TABLE_NAME, ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP)
    createIndex(db, ElectricityMeterLogEntry.TABLE_NAME, ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT)

    val sqlCreateIndex = """
      CREATE UNIQUE INDEX ${ElectricityMeterLogEntry.TABLE_NAME}_unique_index ON ${ElectricityMeterLogEntry.TABLE_NAME}
      (
        ${ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID},
        ${ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP},
        ${ElectricityMeterLogEntry.COLUMN_NAME_PROFILEID}
      )
    """.trimIndent()
    execSQL(db, sqlCreateIndex)
  }

  private fun createElectricityMeterLogView(db: SupportSQLiteDatabase) {
    val sqlCreateEmView = """
      CREATE VIEW ${SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME} AS
        SELECT
          ${ElectricityMeterLogEntry._ID} ${SuplaContract.ElectricityMeterLogViewEntry._ID},
          ${ElectricityMeterLogEntry.COLUMN_NAME_CHANNELID} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID},
          ${ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP},
          datetime(${ElectricityMeterLogEntry.COLUMN_NAME_TIMESTAMP}, 'unixepoch', 'localtime') 
            ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE},
          ${ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED},
          ${ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED},
          ${ElectricityMeterLogEntry.COLUMN_NAME_COMPLEMENT} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_COMPLEMENT},
          ${ElectricityMeterLogEntry.COLUMN_NAME_PROFILEID} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PROFILEID}
        FROM ${ElectricityMeterLogEntry.TABLE_NAME}
    """.trimIndent()
    execSQL(db, sqlCreateEmView)
  }

  private fun createThermostatLogTable(db: SupportSQLiteDatabase) {
    val sqlCreateThlogTable = """
      CREATE TABLE ${ThermostatLogEntry.TABLE_NAME}
      (
        ${ThermostatLogEntry._ID} INTEGER PRIMARY KEY,
        ${ThermostatLogEntry.COLUMN_NAME_CHANNELID} INTEGER NOT NULL,
        ${ThermostatLogEntry.COLUMN_NAME_TIMESTAMP} INTEGER NOT NULL,
        ${ThermostatLogEntry.COLUMN_NAME_ON} INTEGER NOT NULL,
        ${ThermostatLogEntry.COLUMN_NAME_MEASUREDTEMPERATURE} REAL NULL,
        ${ThermostatLogEntry.COLUMN_NAME_PRESETTEMPERATURE} REAL NULL,
        ${ThermostatLogEntry.COLUMN_NAME_PROFILEID} INTEGER NOT NULL
      )
    """.trimIndent()
    execSQL(db, sqlCreateThlogTable)
    createIndex(db, ThermostatLogEntry.TABLE_NAME, ThermostatLogEntry.COLUMN_NAME_CHANNELID)
    createIndex(db, ThermostatLogEntry.TABLE_NAME, ThermostatLogEntry.COLUMN_NAME_TIMESTAMP)

    val sqlCreateIndex = """
      CREATE UNIQUE INDEX ${ThermostatLogEntry.TABLE_NAME}_unique_index ON ${ThermostatLogEntry.TABLE_NAME}
      (
        ${ThermostatLogEntry.COLUMN_NAME_CHANNELID},
        ${ThermostatLogEntry.COLUMN_NAME_TIMESTAMP},
        ${ThermostatLogEntry.COLUMN_NAME_PROFILEID}
      )
    """.trimIndent()
    execSQL(db, sqlCreateIndex)
  }

  private fun createIndex(db: SupportSQLiteDatabase, tableName: String, fieldName: String) {
    val sqlCreateIndex = ("CREATE INDEX ${tableName}_${fieldName}_index ON $tableName ($fieldName)")
    execSQL(db, sqlCreateIndex)
  }
}
