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
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_GROUP_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntity

@Dao
interface ChannelGroupRelationDao {

  @Query(
    """
      SELECT
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelStateEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME relation
      JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} = relation.$COLUMN_GROUP_ID
          AND channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      JOIN ${ChannelEntity.TABLE_NAME} channel
        ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.$COLUMN_CHANNEL_ID
          AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.$COLUMN_CHANNEL_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      LEFT JOIN ${ChannelStateEntity.TABLE_NAME} state
        ON state.${ChannelStateEntity.COLUMN_CHANNEL_ID} = relation.$COLUMN_CHANNEL_ID
          AND state.${ChannelStateEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      WHERE relation.$COLUMN_VISIBLE > 0
          AND channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} > 0  
          AND relation.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      ORDER BY relation.$COLUMN_GROUP_ID
    """
  )
  fun allVisibleRelations(): Single<List<ChannelGroupRelationDataEntity>>

  @Query(
    """
      SELECT
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelStateEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME relation
      JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} = relation.$COLUMN_GROUP_ID
          AND channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      JOIN ${ChannelEntity.TABLE_NAME} channel
        ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.$COLUMN_CHANNEL_ID
          AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.$COLUMN_CHANNEL_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      LEFT JOIN ${ChannelStateEntity.TABLE_NAME} state
        ON state.${ChannelStateEntity.COLUMN_CHANNEL_ID} = relation.$COLUMN_CHANNEL_ID
          AND state.${ChannelStateEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      WHERE relation.$COLUMN_VISIBLE > 0
          AND channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} > 0  
          AND relation.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
          AND relation.$COLUMN_GROUP_ID = :remoteId
    """
  )
  fun findGroupRelationsData(remoteId: Int): Observable<List<ChannelGroupRelationDataEntity>>

  @Query(
    """
      SELECT 
        relation.$COLUMN_ID, relation.$COLUMN_CHANNEL_ID, relation.$COLUMN_GROUP_ID, relation.$COLUMN_VISIBLE, relation.$COLUMN_PROFILE_ID
      FROM $TABLE_NAME relation
      JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} = relation.$COLUMN_GROUP_ID
          AND channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} = relation.$COLUMN_PROFILE_ID
      WHERE relation.$COLUMN_VISIBLE > 0
          AND channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} > 0  
          AND relation.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
          AND relation.$COLUMN_GROUP_ID = :remoteId
    """
  )
  fun findGroupRelations(remoteId: Int): Observable<List<ChannelGroupRelationEntity>>

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  fun deleteByProfile(profileId: Long): Completable
}
