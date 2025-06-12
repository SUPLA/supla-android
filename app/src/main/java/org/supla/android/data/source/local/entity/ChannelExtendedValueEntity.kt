package org.supla.android.data.source.local.entity
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
import org.supla.android.Trace
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity.Companion.TABLE_NAME
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaChannelExtendedValue
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_CHANNEL_ID],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class ChannelExtendedValueEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_VALUE) val value: ByteArray?,
  @ColumnInfo(name = COLUMN_TIMER_START_TIME) val timerStartTime: Date?,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) {

  fun getSuplaValue(): SuplaChannelExtendedValue? {
    return value?.let { toObject(it) } as? SuplaChannelExtendedValue
  }

  private fun toObject(value: ByteArray): Any? {
    try {
      val byteStream = ByteArrayInputStream(value)
      val objectStream = ObjectInputStream(byteStream)
      return objectStream.readObject()
    } catch (e: IOException) {
      Trace.w(TAG, "Could not convert to object (IOException)", e)
    } catch (e: ClassNotFoundException) {
      Trace.w(TAG, "Could not convert to object (ClassNotFoundException)", e)
    }
    return null
  }

  companion object {
    const val TABLE_NAME = "channel_extendedvalue"
    const val COLUMN_ID = "_channel_extendedvalue_id"
    const val COLUMN_CHANNEL_ID = "channelid"
    const val COLUMN_VALUE = "extendedvalue"
    const val COLUMN_TIMER_START_TIME = "timer_start_time"
    const val COLUMN_PROFILE_ID = "profileid"

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_CHANNEL_ID, $COLUMN_VALUE, $COLUMN_TIMER_START_TIME, $COLUMN_PROFILE_ID"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_VALUE BLOB,
          $COLUMN_TIMER_START_TIME INTEGER,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ChannelExtendedValueEntity

    if (id != other.id) return false
    if (channelId != other.channelId) return false
    if (!value.contentEquals(other.value)) return false
    if (timerStartTime != other.timerStartTime) return false
    return profileId == other.profileId
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + channelId
    result = 31 * result + value.contentHashCode()
    result = 31 * result + timerStartTime.hashCode()
    result = 31 * result + profileId.hashCode()
    return result
  }
}
