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
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.TABLE_NAME
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType

@Entity(tableName = TABLE_NAME)
data class WidgetConfigurationEntity(
  @ColumnInfo(name = COLUMN_ID)
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = COLUMN_SUBJECT_ID) val subjectId: Int,
  @ColumnInfo(name = COLUMN_SUBJECT_TYPE) val subjectType: SubjectType,
  @ColumnInfo(name = COLUMN_CAPTION) val caption: String,
  @ColumnInfo(name = COLUMN_ACTION) val action: ActionId,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
  @ColumnInfo(name = COLUMN_GLANCE_ID) val glanceId: String
) {

  companion object {
    const val TABLE_NAME = "widget_configuration"
    const val COLUMN_ID = "id"
    const val COLUMN_SUBJECT_ID = "subject_id"
    const val COLUMN_SUBJECT_TYPE = "subject_type"
    const val COLUMN_CAPTION = "caption"
    const val COLUMN_ACTION = "action"
    const val COLUMN_PROFILE_ID = "profile_id"
    const val COLUMN_GLANCE_ID = "glance_id"

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_SUBJECT_ID, $COLUMN_SUBJECT_TYPE, " +
      "$COLUMN_CAPTION, $COLUMN_ACTION, $COLUMN_PROFILE_ID, $COLUMN_GLANCE_ID"
  }
}
