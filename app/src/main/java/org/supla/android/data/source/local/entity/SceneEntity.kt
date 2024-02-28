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
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.TABLE_NAME
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    androidx.room.Index(
      value = [COLUMN_REMOTE_ID],
      name = "${TABLE_NAME}_${COLUMN_REMOTE_ID}_index"
    ),
    androidx.room.Index(
      value = [COLUMN_LOCATION_ID],
      name = "${TABLE_NAME}_${COLUMN_LOCATION_ID}_index"
    ),
    androidx.room.Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class SceneEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Int,
  @ColumnInfo(name = COLUMN_LOCATION_ID) val locationId: Int,
  @ColumnInfo(name = COLUMN_ALT_ICON) val altIcon: Int,
  @ColumnInfo(name = COLUMN_USER_ICON) val userIcon: Int,
  @ColumnInfo(name = COLUMN_CAPTION) val caption: String,
  @ColumnInfo(name = COLUMN_STARTED_AT) val startedAt: Date?,
  @ColumnInfo(name = COLUMN_ESTIMATED_END_DATE) val estimatedEndDate: Date?,
  @ColumnInfo(name = COLUMN_INITIATOR_ID) val initiatorId: Int?,
  @ColumnInfo(name = COLUMN_INITIATOR_NAME) val initiatorName: String?,
  @ColumnInfo(name = COLUMN_SORT_ORDER) val sortOrder: Int,
  @ColumnInfo(name = COLUMN_VISIBLE) val visible: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: String?
) {

  companion object {
    const val TABLE_NAME = "scene"
    const val COLUMN_ID = "_id"
    const val COLUMN_REMOTE_ID = "sceneid"
    const val COLUMN_LOCATION_ID = "locationid"
    const val COLUMN_ALT_ICON = "alticon"
    const val COLUMN_USER_ICON = "usericon"
    const val COLUMN_CAPTION = "caption"
    const val COLUMN_STARTED_AT = "started_at"
    const val COLUMN_ESTIMATED_END_DATE = "est_end_date"
    const val COLUMN_INITIATOR_ID = "initiator_id"
    const val COLUMN_INITIATOR_NAME = "initiator_name"
    const val COLUMN_SORT_ORDER = "sort_order"
    const val COLUMN_VISIBLE = "visible"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
      CREATE TABLE $TABLE_NAME
      (
        $COLUMN_ID INTEGER PRIMARY KEY,
        $COLUMN_REMOTE_ID INTEGER NOT NULL, 
        $COLUMN_LOCATION_ID INTEGER NOT NULL, 
        $COLUMN_ALT_ICON INTEGER NOT NULL, 
        $COLUMN_USER_ICON INTEGER NOT NULL, 
        $COLUMN_CAPTION TEXT NOT NULL,
        $COLUMN_STARTED_AT INTEGER NULL,
        $COLUMN_ESTIMATED_END_DATE INTEGER NULL,
        $COLUMN_INITIATOR_ID INTEGER NULL,
        $COLUMN_INITIATOR_NAME TEXT NULL,
        $COLUMN_SORT_ORDER INTEGER NOT NULL,
        $COLUMN_VISIBLE INTEGER NOT NULL,
        $COLUMN_PROFILE_ID TEXT NULL
      )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_LOCATION_ID}_index ON $TABLE_NAME ($COLUMN_LOCATION_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )

    const val ALL_COLUMNS = """
      $COLUMN_ID, $COLUMN_REMOTE_ID, $COLUMN_LOCATION_ID, $COLUMN_ALT_ICON, 
      $COLUMN_USER_ICON, $COLUMN_CAPTION, $COLUMN_STARTED_AT, $COLUMN_ESTIMATED_END_DATE,
      $COLUMN_INITIATOR_ID, $COLUMN_INITIATOR_NAME, $COLUMN_SORT_ORDER, $COLUMN_VISIBLE,
      $COLUMN_PROFILE_ID
    """

    val ALL_COLUMNS_ARRAY = arrayOf(
      COLUMN_ID, COLUMN_REMOTE_ID, COLUMN_LOCATION_ID, COLUMN_ALT_ICON,
      COLUMN_USER_ICON, COLUMN_CAPTION, COLUMN_STARTED_AT, COLUMN_ESTIMATED_END_DATE,
      COLUMN_INITIATOR_ID, COLUMN_INITIATOR_NAME, COLUMN_SORT_ORDER, COLUMN_VISIBLE,
      COLUMN_PROFILE_ID
    )
  }
}
