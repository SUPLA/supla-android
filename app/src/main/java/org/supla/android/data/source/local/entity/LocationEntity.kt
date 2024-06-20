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
import org.supla.android.data.source.local.entity.LocationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.LocationEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.LocationEntity.Companion.TABLE_NAME
import org.supla.android.db.Location
import org.supla.android.usecases.location.CollapsedFlag

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_REMOTE_ID],
      name = "${TABLE_NAME}_${COLUMN_REMOTE_ID}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class LocationEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Int,
  @ColumnInfo(name = COLUMN_CAPTION) val caption: String,
  @ColumnInfo(name = COLUMN_VISIBLE) val visible: Int,
  @ColumnInfo(name = COLUMN_COLLAPSED) val collapsed: Int,
  @ColumnInfo(name = COLUMN_SORTING, defaultValue = "DEFAULT") val sorting: Location.SortingType,
  @ColumnInfo(name = COLUMN_SORT_ORDER, defaultValue = "-1") val sortOrder: Int,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
) {

  fun isCollapsed(flag: CollapsedFlag): Boolean = (collapsed and flag.value > 0)

  companion object {
    const val TABLE_NAME = "location"
    const val COLUMN_ID = "_id"
    const val COLUMN_REMOTE_ID = "locationid"
    const val COLUMN_CAPTION = "caption"
    const val COLUMN_VISIBLE = "visible"
    const val COLUMN_COLLAPSED = "collapsed"
    const val COLUMN_SORTING = "sorting"
    const val COLUMN_SORT_ORDER = "sort_order"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_REMOTE_ID INTEGER NOT NULL,
          $COLUMN_CAPTION TEXT NOT NULL,
          $COLUMN_VISIBLE INTEGER NOT NULL,
          $COLUMN_COLLAPSED INTEGER NOT NULL default 0,
          $COLUMN_SORTING TEXT NOT NULL default 'DEFAULT',
          $COLUMN_SORT_ORDER INTEGER NOT NULL DEFAULT -1,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_REMOTE_ID, $COLUMN_CAPTION, " +
      "$COLUMN_VISIBLE, $COLUMN_COLLAPSED, $COLUMN_SORTING, $COLUMN_SORT_ORDER, $COLUMN_PROFILE_ID"
  }
}
