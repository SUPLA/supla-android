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
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_GROUP_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.TABLE_NAME

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_GROUP_ID],
      name = "${TABLE_NAME}_${COLUMN_GROUP_ID}_index"
    ),
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
data class ChannelGroupRelationEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_GROUP_ID) val groupId: Int,
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_VISIBLE) val visible: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long
) {

  companion object {
    const val TABLE_NAME = "channelgroup_rel"
    const val COLUMN_ID = "_id"
    const val COLUMN_GROUP_ID = "groupid"
    const val COLUMN_CHANNEL_ID = "channelid"
    const val COLUMN_VISIBLE = "visible"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_GROUP_ID INTEGER NOT NULL,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_VISIBLE INTEGER NOT NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_GROUP_ID}_index ON $TABLE_NAME ($COLUMN_GROUP_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )
  }
}
