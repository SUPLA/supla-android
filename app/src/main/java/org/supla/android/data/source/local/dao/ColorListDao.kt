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
import androidx.room.Transaction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.data.source.local.entity.ColorEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_GROUP
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_IDX
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.ColorEntity.Companion.COLUMN_TYPE
import org.supla.android.data.source.local.entity.ColorEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ColorEntityType
import org.supla.android.data.source.local.entity.ProfileEntity

@Dao
abstract class ColorListDao {
  @Query(
    """
    SELECT $ALL_COLUMNS 
    FROM $TABLE_NAME 
    WHERE $COLUMN_REMOTE_ID = :remoteId 
      AND $COLUMN_GROUP = :isGroup
      AND $COLUMN_TYPE = :type
      AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
    ORDER BY $COLUMN_IDX ASC, $COLUMN_ID DESC
    """
  )
  abstract fun findAllColors(remoteId: Int, isGroup: Int, type: ColorEntityType): Observable<List<ColorEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun save(color: ColorEntity)

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  abstract suspend fun delete(id: Long)

  @Query(
    """
      UPDATE $TABLE_NAME
      SET $COLUMN_IDX = :position
      WHERE $COLUMN_ID = :id
    """
  )
  abstract suspend fun updatePosition(id: Long, position: Int)

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  abstract suspend fun deleteKtx(remoteId: Int, profileId: Long)

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  abstract fun deleteByProfile(profileId: Long): Completable

  @Transaction
  open suspend fun updatePositions(remoteId: Int, isGroup: Boolean, type: ColorEntityType) {
    val colors = findAllColors(remoteId = remoteId, isGroup = if (isGroup) 1 else 0, type = type).blockingFirst()
    for (i in colors.indices) {
      updatePosition(colors[i].id!!, i)
    }
  }

  @Transaction
  open suspend fun swapPositions(remoteId: Int, from: Int, to: Int, isGroup: Boolean, type: ColorEntityType) {
    val colors = findAllColors(remoteId = remoteId, isGroup = if (isGroup) 1 else 0, type = type).blockingFirst().toMutableList()
    val move = colors.removeAt(from)
    colors.add(to, move)

    for (i in colors.indices) {
      updatePosition(colors[i].id!!, i)
    }
  }
}
