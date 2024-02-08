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
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.NotificationEntity.Companion.TABLE_NAME
import java.time.LocalDateTime

@Entity(tableName = TABLE_NAME)
data class NotificationEntity(
  @ColumnInfo(name = COLUMN_ID)
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @ColumnInfo(name = COLUMN_TITLE) val title: String,
  @ColumnInfo(name = COLUMN_MESSAGE) val message: String,
  @ColumnInfo(name = COLUMN_DATE) val date: LocalDateTime
) {

  companion object {
    const val TABLE_NAME = "notification"
    const val COLUMN_ID = "id"
    const val COLUMN_TITLE = "title"
    const val COLUMN_MESSAGE = "message"
    const val COLUMN_DATE = "date"

    val SQL = """
      CREATE TABLE $TABLE_NAME
      (
        $COLUMN_ID INTEGER NOT NULL PRIMARY KEY,
        $COLUMN_TITLE TEXT NOT NULL,
        $COLUMN_MESSAGE TEXT NOT NULL,
        $COLUMN_DATE INTEGER NOT NULL
      )
    """.trimIndent()

    const val ALL_COLUMNS_STRING = "$COLUMN_ID, $COLUMN_TITLE, $COLUMN_MESSAGE, $COLUMN_DATE"
  }
}
