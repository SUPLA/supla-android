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
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity.Companion.COLUMN_DATE
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity.Companion.TABLE_NAME
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeter
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_CHANNEL_ID],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index"
    ),
    Index(
      value = [COLUMN_DATE],
      name = "${TABLE_NAME}_${COLUMN_DATE}_index"
    ),
    Index(
      value = [COLUMN_CHANNEL_ID, COLUMN_DATE, COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class GeneralPurposeMeterEntity(
  @PrimaryKey
  @ColumnInfo(name = COLUMN_ID)
  val id: Long?,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_DATE) override val date: Date,
  @ColumnInfo(name = COLUMN_VALUE_INCREMENT) val valueIncrement: Float,
  @Deprecated("To remove")
  @ColumnInfo(name = COLUMN_COUNTER_INCREMENT)
  val counterIncrement: Int,
  @ColumnInfo(name = COLUMN_VALUE) val value: Float,
  @Deprecated("To remove")
  @ColumnInfo(name = COLUMN_COUNTER)
  val counter: Int,
  @ColumnInfo(name = COLUMN_MANUALLY_COMPLEMENTED) val manuallyComplemented: Boolean,
  @ColumnInfo(name = COLUMN_COUNTER_RESET) val counterReset: Boolean,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) : BaseLogEntity {

  companion object {
    const val TABLE_NAME = "general_purpose_counter_log"
    const val COLUMN_ID = "id"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_DATE = "date"
    const val COLUMN_VALUE_INCREMENT = "value_increment"
    const val COLUMN_COUNTER_INCREMENT = "counter_increment"
    const val COLUMN_VALUE = "value"
    const val COLUMN_COUNTER = "counter"
    const val COLUMN_MANUALLY_COMPLEMENTED = "manually_complemented"
    const val COLUMN_COUNTER_RESET = "counter_reset"
    const val COLUMN_PROFILE_ID = "profile_id"

    val SQL = arrayOf(
      // Create table
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_DATE INTEGER NOT NULL,
          $COLUMN_VALUE_INCREMENT REAL NOT NULL,
          $COLUMN_COUNTER_INCREMENT INTEGER NOT NULL,
          $COLUMN_VALUE REAL NOT NULL,
          $COLUMN_COUNTER INTEGER NOT NULL,
          $COLUMN_MANUALLY_COMPLEMENTED INTEGER NOT NULL,
          $COLUMN_COUNTER_RESET INTEGER NOT NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        );
      """.trimIndent(),
      // Create indices
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID);",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_DATE}_index ON $TABLE_NAME ($COLUMN_DATE);",
      """
        CREATE UNIQUE INDEX ${TABLE_NAME}_unique_index ON $TABLE_NAME
          ($COLUMN_CHANNEL_ID, $COLUMN_DATE, $COLUMN_PROFILE_ID);
      """.trimIndent()
    )

    const val ALL_COLUMNS = """
      $COLUMN_ID, $COLUMN_CHANNEL_ID, $COLUMN_DATE, $COLUMN_VALUE_INCREMENT,
      $COLUMN_COUNTER_INCREMENT, $COLUMN_VALUE, $COLUMN_COUNTER, $COLUMN_MANUALLY_COMPLEMENTED,
      $COLUMN_COUNTER_RESET, $COLUMN_PROFILE_ID
    """

    fun create(
      entry: GeneralPurposeMeter,
      channelId: Int,
      date: Date = entry.date,
      valueIncrement: Float = entry.value,
      counterIncrement: Int = 0,
      value: Float = entry.value,
      counter: Int = 0,
      profileId: Long,
      id: Long? = null,
      manuallyComplemented: Boolean = false,
      counterReset: Boolean = false
    ) =
      GeneralPurposeMeterEntity(
        id,
        channelId,
        date,
        valueIncrement,
        counterIncrement,
        value,
        counter,
        manuallyComplemented,
        counterReset,
        profileId
      )
  }
}
