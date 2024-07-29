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
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.SuplaContract
import org.supla.android.db.room.SqlExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementsLegacySchema @Inject constructor() : SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  fun onCreate(db: SupportSQLiteDatabase) {
    // Create views at the end
    createImpulseCounterLogView(db)
    createElectricityMeterLogView(db)
  }

  private fun createImpulseCounterLogView(db: SupportSQLiteDatabase) {
    val sqlCreateIcView = """
      CREATE VIEW ${SuplaContract.ImpulseCounterLogViewEntry.VIEW_NAME} AS
        SELECT 
          ${ImpulseCounterLogEntity.COLUMN_ID} ${SuplaContract.ImpulseCounterLogViewEntry._ID},
          ${ImpulseCounterLogEntity.COLUMN_CHANNEL_ID} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CHANNELID},
          ${ImpulseCounterLogEntity.COLUMN_TIMESTAMP} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_TIMESTAMP},
          datetime(${ImpulseCounterLogEntity.COLUMN_TIMESTAMP}, 'unixepoch', 'localtime') 
            ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_DATE},
          ${ImpulseCounterLogEntity.COLUMN_COUNTER} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COUNTER},
          ${ImpulseCounterLogEntity.COLUMN_CALCULATED_VALUE} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_CALCULATEDVALUE},
          ${ImpulseCounterLogEntity.COLUMN_MANUALLY_COMPLEMENTED} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_COMPLEMENT},
          ${ImpulseCounterLogEntity.COLUMN_PROFILE_ID} ${SuplaContract.ImpulseCounterLogViewEntry.COLUMN_NAME_PROFILEID}
         FROM ${ImpulseCounterLogEntity.TABLE_NAME}
    """.trimIndent()

    execSQL(db, sqlCreateIcView)
  }

  private fun createElectricityMeterLogView(db: SupportSQLiteDatabase) {
    val sqlCreateEmView = """
      CREATE VIEW ${SuplaContract.ElectricityMeterLogViewEntry.VIEW_NAME} AS
        SELECT
          ${ElectricityMeterLogEntity.COLUMN_ID} ${SuplaContract.ElectricityMeterLogViewEntry._ID},
          ${ElectricityMeterLogEntity.COLUMN_CHANNEL_ID} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_CHANNELID},
          ${ElectricityMeterLogEntity.COLUMN_TIMESTAMP} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_TIMESTAMP},
          datetime(${ElectricityMeterLogEntity.COLUMN_TIMESTAMP}, 'unixepoch', 'localtime') 
            ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_DATE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE1_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_FAE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE2_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_FAE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE3_FAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_FAE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE1_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE1_RAE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE2_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE2_RAE},
          ${ElectricityMeterLogEntity.COLUMN_PHASE3_RAE} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PHASE3_RAE},
          ${ElectricityMeterLogEntity.COLUMN_FAE_BALANCED} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_FAE_BALANCED},
          ${ElectricityMeterLogEntity.COLUMN_RAE_BALANCED} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_RAE_BALANCED},
          ${ElectricityMeterLogEntity.COLUMN_MANUALLY_COMPLEMENTED} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_COMPLEMENT},
          ${ElectricityMeterLogEntity.COLUMN_PROFILE_ID} ${SuplaContract.ElectricityMeterLogViewEntry.COLUMN_NAME_PROFILEID}
        FROM ${ElectricityMeterLogEntity.TABLE_NAME}
    """.trimIndent()
    execSQL(db, sqlCreateEmView)
  }
}
