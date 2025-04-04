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
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_ALT_ICON
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_FLAGS
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_FUNCTION
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_ONLINE
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_POSITION
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_TOTAL_VALUE
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_USER_ICON
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.ChannelGroupEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary

@Dao
interface ChannelGroupDao {

  @Query(
    """
    SELECT $ALL_COLUMNS FROM $TABLE_NAME
    WHERE
      $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND $COLUMN_REMOTE_ID = :remoteId
    """
  )
  fun findByRemoteId(remoteId: Int): Maybe<ChannelGroupEntity>

  @Query(
    """
      SELECT
        channel_group.$COLUMN_ID group_$COLUMN_ID,
        channel_group.$COLUMN_REMOTE_ID group_$COLUMN_REMOTE_ID,
        channel_group.$COLUMN_CAPTION group_$COLUMN_CAPTION,
        channel_group.$COLUMN_ONLINE group_$COLUMN_ONLINE,
        channel_group.$COLUMN_FUNCTION group_$COLUMN_FUNCTION,
        channel_group.$COLUMN_VISIBLE group_$COLUMN_VISIBLE,
        channel_group.$COLUMN_LOCATION_ID group_$COLUMN_LOCATION_ID,
        channel_group.$COLUMN_ALT_ICON group_$COLUMN_ALT_ICON,
        channel_group.$COLUMN_USER_ICON group_$COLUMN_USER_ICON,
        channel_group.$COLUMN_FLAGS group_$COLUMN_FLAGS,
        channel_group.$COLUMN_TOTAL_VALUE group_$COLUMN_TOTAL_VALUE,
        channel_group.$COLUMN_POSITION group_$COLUMN_POSITION,
        channel_group.$COLUMN_PROFILE_ID group_$COLUMN_PROFILE_ID,
        location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
        location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
        location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
        location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
        location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
        location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
        location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME channel_group
      JOIN ${LocationEntity.TABLE_NAME} location
        ON channel_group.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
          AND channel_group.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
      WHERE channel_group.$COLUMN_VISIBLE > 0
        AND channel_group.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      ORDER BY 
        location.${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_CAPTION} COLLATE LOCALIZED,
        channel_group.$COLUMN_POSITION,
        channel_group.$COLUMN_FUNCTION DESC,
        channel_group.$COLUMN_CAPTION COLLATE LOCALIZED
    """
  )
  fun findList(): Single<List<ChannelGroupDataEntity>>

  @Query(
    """
      SELECT
        channel_group.$COLUMN_ID group_$COLUMN_ID,
        channel_group.$COLUMN_REMOTE_ID group_$COLUMN_REMOTE_ID,
        channel_group.$COLUMN_CAPTION group_$COLUMN_CAPTION,
        channel_group.$COLUMN_ONLINE group_$COLUMN_ONLINE,
        channel_group.$COLUMN_FUNCTION group_$COLUMN_FUNCTION,
        channel_group.$COLUMN_VISIBLE group_$COLUMN_VISIBLE,
        channel_group.$COLUMN_LOCATION_ID group_$COLUMN_LOCATION_ID,
        channel_group.$COLUMN_ALT_ICON group_$COLUMN_ALT_ICON,
        channel_group.$COLUMN_USER_ICON group_$COLUMN_USER_ICON,
        channel_group.$COLUMN_FLAGS group_$COLUMN_FLAGS,
        channel_group.$COLUMN_TOTAL_VALUE group_$COLUMN_TOTAL_VALUE,
        channel_group.$COLUMN_POSITION group_$COLUMN_POSITION,
        channel_group.$COLUMN_PROFILE_ID group_$COLUMN_PROFILE_ID,
        location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
        location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
        location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
        location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
        location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
        location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
        location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME channel_group
      JOIN ${LocationEntity.TABLE_NAME} location
        ON channel_group.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
      WHERE channel_group.$COLUMN_REMOTE_ID = :groupRemoteId
        AND channel_group.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
    """
  )
  fun findGroupDataEntity(groupRemoteId: Int): Maybe<ChannelGroupDataEntity>

  @Query(
    """
      SELECT
        (
          SELECT COUNT(relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID})
          FROM ${ChannelGroupRelationEntity.TABLE_NAME} relation
          JOIN ${ChannelValueEntity.TABLE_NAME} value
            ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
              AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
          WHERE relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID} = channel_group.$COLUMN_REMOTE_ID
            AND relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID} = channel_group.$COLUMN_PROFILE_ID
            AND relation.${ChannelGroupRelationEntity.COLUMN_VISIBLE} > 0
            AND value.${ChannelValueEntity.COLUMN_ONLINE} = 1
          GROUP BY relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
        ) channel_online_count,
        (
          SELECT COUNT(relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID})
          FROM ${ChannelGroupRelationEntity.TABLE_NAME} relation
          WHERE relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID} = channel_group.$COLUMN_REMOTE_ID
            AND relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID} = channel_group.$COLUMN_PROFILE_ID
            AND relation.${ChannelGroupRelationEntity.COLUMN_VISIBLE} > 0
          GROUP BY relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
        ) channel_count
      FROM $TABLE_NAME channel_group
      WHERE $COLUMN_ID = :groupId
    """
  )
  fun findGroupOnlineCount(groupId: Long): Maybe<GroupOnlineSummary>

  @Query(
    """
      SELECT
        channel_group.$COLUMN_ID group_$COLUMN_ID,
        channel_group.$COLUMN_REMOTE_ID group_$COLUMN_REMOTE_ID,
        channel_group.$COLUMN_CAPTION group_$COLUMN_CAPTION,
        channel_group.$COLUMN_ONLINE group_$COLUMN_ONLINE,
        channel_group.$COLUMN_FUNCTION group_$COLUMN_FUNCTION,
        channel_group.$COLUMN_VISIBLE group_$COLUMN_VISIBLE,
        channel_group.$COLUMN_LOCATION_ID group_$COLUMN_LOCATION_ID,
        channel_group.$COLUMN_ALT_ICON group_$COLUMN_ALT_ICON,
        channel_group.$COLUMN_USER_ICON group_$COLUMN_USER_ICON,
        channel_group.$COLUMN_FLAGS group_$COLUMN_FLAGS,
        channel_group.$COLUMN_TOTAL_VALUE group_$COLUMN_TOTAL_VALUE,
        channel_group.$COLUMN_POSITION group_$COLUMN_POSITION,
        channel_group.$COLUMN_PROFILE_ID group_$COLUMN_PROFILE_ID,
        location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
        location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
        location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
        location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
        location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
        location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
        location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME channel_group
      JOIN ${LocationEntity.TABLE_NAME} location
        ON channel_group.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
          AND channel_group.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
      WHERE channel_group.$COLUMN_VISIBLE > 0
        AND channel_group.$COLUMN_PROFILE_ID = :profileId
      ORDER BY 
        location.${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_CAPTION} COLLATE LOCALIZED,
        channel_group.$COLUMN_POSITION,
        channel_group.$COLUMN_FUNCTION DESC,
        channel_group.$COLUMN_CAPTION COLLATE LOCALIZED
    """
  )
  fun findProfileGroups(profileId: Long): Single<List<ChannelGroupDataEntity>>

  @Update
  fun update(groups: List<ChannelGroupEntity>): Completable

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>

  @Query("UPDATE $TABLE_NAME SET $COLUMN_CAPTION = :caption WHERE $COLUMN_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable
}
