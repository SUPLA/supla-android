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
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_TIMESTAMP
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.TABLE_NAME
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [ COLUMN_CHANNEL_ID ],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index"
    ),
    Index(
      value = [ COLUMN_TIMESTAMP ],
      name = "${TABLE_NAME}_${COLUMN_TIMESTAMP}_index"
    ),
    Index(
      value = [ COLUMN_CHANNEL_ID, COLUMN_TIMESTAMP, COLUMN_PROFILE_ID ],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class TemperatureAndHumidityLogEntity(
  @PrimaryKey
  @ColumnInfo(name = COLUMN_ID)
  val id: Long?,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_TIMESTAMP) override val date: Date,
  @ColumnInfo(name = COLUMN_TEMPERATURE) override val temperature: Float?,
  @ColumnInfo(name = COLUMN_HUMIDITY) override val humidity: Float?,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) : BaseTemperatureEntity, BaseHumidityEntity {

  companion object {
    const val TABLE_NAME = "temphumidity_log"
    const val COLUMN_ID = "_temphumidit_id"
    const val COLUMN_CHANNEL_ID = "channelid"
    const val COLUMN_TIMESTAMP = "date"
    const val COLUMN_TEMPERATURE = "temperature"
    const val COLUMN_HUMIDITY = "humidity"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_TIMESTAMP INTEGER NOT NULL,
          $COLUMN_TEMPERATURE REAL NULL,
          $COLUMN_HUMIDITY REAL NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        );
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID);",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_TIMESTAMP}_index ON $TABLE_NAME ($COLUMN_TIMESTAMP);",
      """
        CREATE UNIQUE INDEX ${TABLE_NAME}_unique_index ON $TABLE_NAME
          ($COLUMN_CHANNEL_ID, $COLUMN_TIMESTAMP, $COLUMN_PROFILE_ID);
      """.trimIndent()
    )

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_CHANNEL_ID, $COLUMN_TIMESTAMP, $COLUMN_TEMPERATURE, $COLUMN_HUMIDITY, $COLUMN_PROFILE_ID"
  }
}
