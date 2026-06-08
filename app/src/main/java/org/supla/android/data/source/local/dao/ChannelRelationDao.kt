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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_CHANNEL_RELATION_TYPE
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_PARENT_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity

@Dao
interface ChannelRelationDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(channelRelation: ChannelRelationEntity): Completable

  @Query(
    "DELETE FROM $TABLE_NAME WHERE ($COLUMN_CHANNEL_ID = :remoteId OR $COLUMN_PARENT_ID = :remoteId) AND $COLUMN_PROFILE_ID = :profileId"
  )
  suspend fun deleteKtx(remoteId: Int, profileId: Long)

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}")
  fun getForActiveProfile(): Observable<List<ChannelRelationEntity>>

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  fun getForProfile(profileId: Long): Observable<List<ChannelRelationEntity>>

  @Query(
    """
      SELECT $ALL_COLUMNS FROM $TABLE_NAME 
        WHERE $COLUMN_PROFILE_ID = :profileId 
        AND $COLUMN_PARENT_ID = :parentId
    """
  )
  fun findChildren(profileId: Long, parentId: Int): Observable<List<ChannelRelationEntity>>

  @Query(
    """
      UPDATE $TABLE_NAME 
        SET ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1 
        WHERE $COLUMN_PROFILE_ID = :profileId 
    """
  )
  fun markAsRemovable(profileId: Long): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1")
  fun cleanUnused(): Completable

  @Query(
    """
    SELECT 
      ${ChannelRelationEntity.JOIN_COLUMNS},
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
    FROM $TABLE_NAME relation
    JOIN ${ChannelEntity.TABLE_NAME} channel
      ON relation.$COLUMN_CHANNEL_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
        AND relation.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
    JOIN ${ChannelValueEntity.TABLE_NAME} value
      ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID}
        AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = value.${ChannelValueEntity.COLUMN_PROFILE_ID}
    JOIN ${LocationEntity.TABLE_NAME} location
      ON channel.${ChannelEntity.COLUMN_LOCATION_ID} = location.${LocationEntity.COLUMN_REMOTE_ID}
        AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = location.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelConfigEntity.TABLE_NAME} config
      ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = config.${ChannelConfigEntity.COLUMN_CHANNEL_ID}
        AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = config.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelExtendedValueEntity.TABLE_NAME} extended_value
      ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = extended_value.${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID}
        AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = extended_value.${ChannelExtendedValueEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelStateEntity.TABLE_NAME} state
      ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = state.${ChannelStateEntity.COLUMN_CHANNEL_ID}
        AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = state.${ChannelStateEntity.COLUMN_PROFILE_ID}
    WHERE relation.$COLUMN_PARENT_ID = :parentRemoteId
      AND relation.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
  """
  )
  fun findChildrenFor(parentRemoteId: Int): Maybe<List<ChannelChildEntity>>

  @Query("SELECT COUNT($COLUMN_CHANNEL_RELATION_TYPE) FROM $TABLE_NAME")
  fun count(): Observable<Int>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  fun deleteByProfile(profileId: Long): Completable

  @Query(
    """
    SELECT 
      $COLUMN_PARENT_ID,
      $COLUMN_CHANNEL_ID,
      $COLUMN_CHANNEL_RELATION_TYPE,
      ${ChannelRelationEntity.COLUMN_DELETE_FLAG},
      $COLUMN_PROFILE_ID
    FROM $TABLE_NAME
    WHERE $COLUMN_CHANNEL_ID = :childId
      AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
  """
  )
  fun findParentsOf(childId: Int): Single<List<ChannelRelationEntity>>
}
