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
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.COLUMN_DATE
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.COLUMN_TAG_ID
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.TABLE_NAME
import java.util.Date

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_TAG_ID],
      name = "${TABLE_NAME}_${COLUMN_TAG_ID}_index"
    ),
    Index(
      value = [COLUMN_DATE],
      name = "${TABLE_NAME}_${COLUMN_DATE}_index"
    )
  ]
)
data class NfcCallEntity(
  @ColumnInfo(name = COLUMN_ID)
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = COLUMN_TAG_ID) val tagId: Long,
  @ColumnInfo(name = COLUMN_DATE) val date: Date,
  @ColumnInfo(name = COLUMN_RESULT) val result: NfcCallResult
) {

  companion object {
    const val TABLE_NAME = "nfc_call_entity"
    const val COLUMN_ID = "id"
    const val COLUMN_TAG_ID = "tag_id"
    const val COLUMN_DATE = "date"
    const val COLUMN_RESULT = "result"

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_TAG_ID, $COLUMN_DATE, $COLUMN_RESULT"
  }
}

enum class NfcCallResult(val value: Int) {
  SUCCESS(1),
  FAILURE(2),
  ACTION_MISSING(3),
  TAG_ADDED(4);

  companion object {
    fun from(value: Int): NfcCallResult =
      when (value) {
        1 -> SUCCESS
        3 -> ACTION_MISSING
        4 -> TAG_ADDED
        else -> FAILURE
      }
  }
}
