package org.supla.android.db.room.measurements.migrations/*
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
import org.supla.android.Trace
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.SuplaContract
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_FAE_BALANCED
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_FRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE1_RRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_FRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE2_RRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_FRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RAE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_PHASE3_RRE
import org.supla.android.db.SuplaContract.ElectricityMeterLogEntry.COLUMN_NAME_RAE_BALANCED
import org.supla.android.db.SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_CALCULATEDVALUE
import org.supla.android.db.SuplaContract.ImpulseCounterLogEntry.COLUMN_NAME_COUNTER
import org.supla.android.db.room.SqlExecutor
import org.supla.android.extensions.TAG

val MEASUREMENTS_DB_MIGRATION_31_32: Migration = object : Migration(31, 32), SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  override fun migrate(database: SupportSQLiteDatabase) {
    execSQL(database, GeneralPurposeMeterEntity.SQL)
    execSQL(database, GeneralPurposeMeasurementEntity.SQL)

    try {
      findAndRemoveChannelsWithNegativeLogEntries(database, SuplaContract.ElectricityMeterLogEntry.TABLE_NAME, emColumns)
      findAndRemoveChannelsWithNegativeLogEntries(database, SuplaContract.ImpulseCounterLogEntry.TABLE_NAME, icColumns)
    } catch (ex: Exception) {
      Trace.e(TAG, "Removing data with negative log entries failed!", ex)
    }
  }

  /**
   * Starting from DB version 32 we introduced proper counter reset handling. To correct existing data it is needed to recalculate all
   * differences in channels where negative values are stored. For that all such channels data are removed and will be recalculated when
   * user again open the chart.
   */
  private fun findAndRemoveChannelsWithNegativeLogEntries(
    database: SupportSQLiteDatabase,
    tableName: String,
    columnsForWhere: Array<String>
  ) {
    val channelsAndProfiles = mutableListOf<Pair<Long, Int>>()
    query(database, channelsListSql(tableName, columnsForWhere)).use { cursor ->
      if (!cursor.moveToFirst()) {
        return
      }

      do {
        val channelId = cursor.getInt(0)
        val profileId = cursor.getLong(1)
        channelsAndProfiles.add(Pair(profileId, channelId))
      } while (cursor.moveToNext())
    }

    channelsAndProfiles.forEach {
      execSQL(database, deleteEntriesSql(tableName, it.first, it.second))
    }
  }
}

private val emColumns = arrayOf(
  COLUMN_NAME_PHASE1_FAE,
  COLUMN_NAME_PHASE1_RAE,
  COLUMN_NAME_PHASE1_FRE,
  COLUMN_NAME_PHASE1_RRE,
  COLUMN_NAME_PHASE2_FAE,
  COLUMN_NAME_PHASE2_RAE,
  COLUMN_NAME_PHASE2_FRE,
  COLUMN_NAME_PHASE2_RRE,
  COLUMN_NAME_PHASE3_FAE,
  COLUMN_NAME_PHASE3_RAE,
  COLUMN_NAME_PHASE3_FRE,
  COLUMN_NAME_PHASE3_RRE,
  COLUMN_NAME_FAE_BALANCED,
  COLUMN_NAME_RAE_BALANCED
)

private val icColumns = arrayOf(
  COLUMN_NAME_COUNTER,
  COLUMN_NAME_CALCULATEDVALUE
)

private fun channelsListSql(tableName: String, columnsForWhere: Array<String>) = """
    SELECT channelid, profileid 
    FROM $tableName 
    WHERE ${columnsForWhere.joinToString(separator = " < 0 OR ")} < 0
    GROUP BY channelid, profileid
""".trimIndent()

private fun deleteEntriesSql(tableName: String, profileId: Long, channelId: Int) = """
    DELETE FROM $tableName
    WHERE profileId = $profileId AND channelid = $channelId
""".trimIndent()
