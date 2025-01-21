package org.supla.android.data.source.local.entity.measurements
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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity.Companion.COLUMN_PHASE
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity.Companion.COLUMN_TIMESTAMP
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity.Companion.TABLE_NAME
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_CHANNEL_ID],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index"
    ),
    Index(
      value = [COLUMN_TIMESTAMP],
      name = "${TABLE_NAME}_${COLUMN_TIMESTAMP}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    ),
    Index(
      value = [COLUMN_CHANNEL_ID, COLUMN_TIMESTAMP, COLUMN_PHASE, COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class PowerActiveHistoryLogEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = COLUMN_ID)
  val id: Long,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_TIMESTAMP) override val date: Date,
  @ColumnInfo(name = COLUMN_PHASE) val phase: Phase,
  @ColumnInfo(name = COLUMN_MIN) override val min: Float,
  @ColumnInfo(name = COLUMN_MAX) override val max: Float,
  @ColumnInfo(name = COLUMN_AVG) override val avg: Float,
  @ColumnInfo(name = COLUMN_GROUPING_STRING) override val groupingString: String,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) : ElectricityBaseLogEntity {

  companion object {
    const val TABLE_NAME = "power_active_history_log"
    const val COLUMN_ID = "id"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_TIMESTAMP = "timestamp"
    const val COLUMN_PHASE = "phase"
    const val COLUMN_MIN = "min"
    const val COLUMN_MAX = "max"
    const val COLUMN_AVG = "avg"
    const val COLUMN_GROUPING_STRING = "grouping_string"
    const val COLUMN_PROFILE_ID = "profile_id"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER NOT NULL PRIMARY KEY,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_TIMESTAMP INTEGER NOT NULL,
          $COLUMN_PHASE INTEGER NOT NULL,
          $COLUMN_MIN REAL NOT NULL,
          $COLUMN_MAX REAL NOT NULL,
          $COLUMN_AVG REAL NOT NULL,
          $COLUMN_GROUPING_STRING TEXT NOT NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID);",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_TIMESTAMP}_index ON $TABLE_NAME ($COLUMN_TIMESTAMP);",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID);",
      """
        CREATE UNIQUE INDEX ${TABLE_NAME}_unique_index 
        ON $TABLE_NAME ($COLUMN_CHANNEL_ID, $COLUMN_TIMESTAMP, $COLUMN_PHASE, $COLUMN_PROFILE_ID);
      """.trimIndent()
    )

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_CHANNEL_ID, $COLUMN_TIMESTAMP, $COLUMN_PHASE, " +
      "$COLUMN_MIN, $COLUMN_MAX, $COLUMN_AVG, $COLUMN_GROUPING_STRING, $COLUMN_PROFILE_ID"
  }
}
