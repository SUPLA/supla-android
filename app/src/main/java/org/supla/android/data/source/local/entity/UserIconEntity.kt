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
import org.supla.android.data.source.local.entity.UserIconEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.UserIconEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.UserIconEntity.Companion.TABLE_NAME

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
    ),
    Index(
      value = [COLUMN_REMOTE_ID, COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_unique_index",
      unique = true
    )
  ]
)
data class UserIconEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_REMOTE_ID) val remoteId: Int,
  @ColumnInfo(name = COLUMN_IMAGE_1) val image1: ByteArray?,
  @ColumnInfo(name = COLUMN_IMAGE_2) val image2: ByteArray?,
  @ColumnInfo(name = COLUMN_IMAGE_3) val image3: ByteArray?,
  @ColumnInfo(name = COLUMN_IMAGE_4) val image4: ByteArray?,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,

) {
  companion object {
    const val TABLE_NAME = "user_icons"
    const val COLUMN_ID = "_id"
    const val COLUMN_REMOTE_ID = "remoteid"
    const val COLUMN_IMAGE_1 = "uimage1"
    const val COLUMN_IMAGE_2 = "image2"
    const val COLUMN_IMAGE_3 = "image3"
    const val COLUMN_IMAGE_4 = "image4"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_REMOTE_ID INTEGER NOT NULL,
          $COLUMN_IMAGE_1 BLOB,
          $COLUMN_IMAGE_2 BLOB,
          $COLUMN_IMAGE_3 BLOB,
          $COLUMN_IMAGE_4 BLOB,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)",
      "CREATE UNIQUE INDEX ${TABLE_NAME}_unique_index ON $TABLE_NAME ($COLUMN_REMOTE_ID, $COLUMN_PROFILE_ID)",
    )

    const val ALL_COLUMNS = "$COLUMN_ID, $COLUMN_REMOTE_ID, $COLUMN_IMAGE_1, " +
      "$COLUMN_IMAGE_2, $COLUMN_IMAGE_3, $COLUMN_IMAGE_4, $COLUMN_PROFILE_ID"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UserIconEntity

    if (id != other.id) return false
    if (remoteId != other.remoteId) return false
    if (!image1.contentEquals(other.image1)) return false
    if (!image2.contentEquals(other.image2)) return false
    if (!image3.contentEquals(other.image3)) return false
    if (!image4.contentEquals(other.image4)) return false
    return profileId == other.profileId
  }

  override fun hashCode(): Int {
    var result = id?.hashCode() ?: 0
    result = 31 * result + remoteId
    result = 31 * result + image1.contentHashCode()
    result = 31 * result + image2.contentHashCode()
    result = 31 * result + image3.contentHashCode()
    result = 31 * result + image4.contentHashCode()
    result = 31 * result + profileId.hashCode()
    return result
  }
}
