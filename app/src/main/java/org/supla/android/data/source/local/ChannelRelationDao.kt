package org.supla.android.data.source.local
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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.entity.ChannelRelationEntity

@Dao
interface ChannelRelationDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(channelRelation: ChannelRelationEntity): Completable

  @Query("SELECT * FROM ${ChannelRelationEntity.TABLE_NAME} WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = :profileId")
  fun getForProfile(profileId: Long): Observable<List<ChannelRelationEntity>>

  @Query(
    """
      SELECT * FROM ${ChannelRelationEntity.TABLE_NAME} 
        WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = :profileId 
        AND ${ChannelRelationEntity.COLUMN_PARENT_ID} = :parentId
    """
  )
  fun findChildren(profileId: Long, parentId: Int): Observable<List<ChannelRelationEntity>>

  @Query(
    """
      UPDATE ${ChannelRelationEntity.TABLE_NAME} 
        SET ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1 
        WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = :profileId 
    """
  )
  fun markAsRemovable(profileId: Long): Completable

  @Query("DELETE FROM ${ChannelRelationEntity.TABLE_NAME} WHERE ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1")
  fun cleanUnused(): Completable
}
