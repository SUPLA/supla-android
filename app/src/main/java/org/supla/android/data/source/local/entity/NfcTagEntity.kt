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
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.TABLE_NAME
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType

@Entity(tableName = TABLE_NAME)
data class NfcTagEntity(
  @ColumnInfo(name = COLUMN_ID)
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = COLUMN_UUID) val uuid: String,
  @ColumnInfo(name = COLUMN_NAME) val name: String,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long? = null,
  @ColumnInfo(name = COLUMN_SUBJECT_TYPE) val subjectType: SubjectType? = null,
  @ColumnInfo(name = COLUMN_SUBJECT_ID) val subjectId: Int? = null,
  @ColumnInfo(name = COLUMN_ACTION_ID) val actionId: ActionId? = null,
  @ColumnInfo(name = COLUMN_READ_ONLY) val readOnly: Boolean
) {
  val configuration: Configuration?
    get() =
      if (profileId != null && subjectType != null && subjectId != null && actionId != null) {
        Configuration(profileId, subjectType, subjectId, actionId)
      } else {
        null
      }

  data class Configuration(
    val profileId: Long,
    val subjectType: SubjectType,
    val subjectId: Int,
    val actionId: ActionId
  )

  companion object {
    const val TABLE_NAME = "nfc_tag_entity"
    const val COLUMN_ID = "id"
    const val COLUMN_UUID = "uuid"
    const val COLUMN_NAME = "name"
    const val COLUMN_PROFILE_ID = "profile_id"
    const val COLUMN_SUBJECT_TYPE = "subject_type"
    const val COLUMN_SUBJECT_ID = "subject_id"
    const val COLUMN_ACTION_ID = "action_id"
    const val COLUMN_READ_ONLY = "read_only"

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_UUID, $COLUMN_NAME, " +
      "$COLUMN_PROFILE_ID, $COLUMN_SUBJECT_TYPE, $COLUMN_SUBJECT_ID, $COLUMN_ACTION_ID, $COLUMN_READ_ONLY"
  }
}
