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
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.TABLE_NAME

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_REMOTE_ID],
      name = "${TABLE_NAME}_${COLUMN_REMOTE_ID}_index"
    ),
    Index(
      value = [COLUMN_LOCATION_ID],
      name = "${TABLE_NAME}_${COLUMN_LOCATION_ID}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class ChannelGroupEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Int,
  @ColumnInfo(name = COLUMN_CAPTION) val caption: String,
  @ColumnInfo(name = COLUMN_ONLINE) val online: Boolean,
  @ColumnInfo(name = COLUMN_FUNCTION) val function: Int,
  @ColumnInfo(name = COLUMN_VISIBLE) val visible: Int,
  @ColumnInfo(name = COLUMN_LOCATION_ID) val locationId: Int,
  @ColumnInfo(name = COLUMN_ALT_ICON) val altIcon: Int,
  @ColumnInfo(name = COLUMN_USER_ICON) val userIcon: Int,
  @ColumnInfo(name = COLUMN_FLAGS) val flags: Int,
  @ColumnInfo(name = COLUMN_TOTAL_VALUE) val totalValue: String?,
  @ColumnInfo(name = COLUMN_POSITION, defaultValue = "0") val position: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) {

  companion object {
    const val TABLE_NAME = "channelgroup"
    const val COLUMN_ID = "_id"
    const val COLUMN_REMOTE_ID = "groupid"
    const val COLUMN_CAPTION = "caption"
    const val COLUMN_ONLINE = "online"
    const val COLUMN_FUNCTION = "func"
    const val COLUMN_VISIBLE = "visible"
    const val COLUMN_LOCATION_ID = "locatonid"
    const val COLUMN_ALT_ICON = "alticon"
    const val COLUMN_USER_ICON = "usericon"
    const val COLUMN_FLAGS = "flags"
    const val COLUMN_TOTAL_VALUE = "totalvalue"
    const val COLUMN_POSITION = "position"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_REMOTE_ID INTEGER NOT NULL,
          $COLUMN_CAPTION TEXT NOT NULL,
          $COLUMN_ONLINE INTEGER NOT NULL,
          $COLUMN_FUNCTION INTEGER NOT NULL,
          $COLUMN_VISIBLE INTEGER NOT NULL,
          $COLUMN_LOCATION_ID INTEGER NOT NULL,
          $COLUMN_ALT_ICON INTEGER NOT NULL,
          $COLUMN_USER_ICON INTEGER NOT NULL,
          $COLUMN_FLAGS INTEGER NOT NULL,
          $COLUMN_TOTAL_VALUE TEXT,
          $COLUMN_POSITION INTEGER NOT NULL default 0,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_LOCATION_ID}_index ON $TABLE_NAME ($COLUMN_LOCATION_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )
  }
}
