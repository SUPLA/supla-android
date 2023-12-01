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
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_GROUP
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.ColorEntity.Companion.TABLE_NAME

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_REMOTE_ID],
      name = "${TABLE_NAME}_${COLUMN_REMOTE_ID}_index"
    ),
    Index(
      value = [COLUMN_GROUP],
      name = "${TABLE_NAME}_${COLUMN_GROUP}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class ColorEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Int,
  @ColumnInfo(name = COLUMN_GROUP) val isGroup: Boolean,
  @ColumnInfo(name = COLUMN_IDX) val idx: Int,
  @ColumnInfo(name = COLUMN_COLOR) val color: Int,
  @ColumnInfo(name = COLUMN_BRIGHTNESS) val brightness: Short,
  @ColumnInfo(name = COLUMN_PROFILE_ID, defaultValue = "1") val profileId: Long
) {

  companion object {
    const val TABLE_NAME = "color_list_item"
    const val COLUMN_ID = "_id"
    const val COLUMN_REMOTE_ID = "remoteid"
    const val COLUMN_GROUP = "isgroup"
    const val COLUMN_IDX = "idx"
    const val COLUMN_COLOR = "color"
    const val COLUMN_BRIGHTNESS = "brightness"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_REMOTE_ID INTEGER NOT NULL,
          $COLUMN_GROUP INTEGER NOT NULL,
          $COLUMN_IDX INTEGER NOT NULL,
          $COLUMN_COLOR INTEGER NOT NULL,
          $COLUMN_BRIGHTNESS INTEGER NOT NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_GROUP}_index ON $TABLE_NAME ($COLUMN_GROUP)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )
  }
}
