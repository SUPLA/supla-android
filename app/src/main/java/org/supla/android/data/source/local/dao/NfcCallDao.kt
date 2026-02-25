package org.supla.android.data.source.local.dao
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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.supla.android.data.source.local.entity.NfcCallEntity
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.COLUMN_DATE
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.COLUMN_TAG_ID
import org.supla.android.data.source.local.entity.NfcCallEntity.Companion.TABLE_NAME

@Dao
interface NfcCallDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(entity: NfcCallEntity): Long

  @Query(
    """
      SELECT $ALL_COLUMNS from $TABLE_NAME
      WHERE $COLUMN_TAG_ID = :tagId
      ORDER BY $COLUMN_DATE DESC
      LIMIT 10
    """
  )
  suspend fun findLastForId(tagId: Long): List<NfcCallEntity>
}
