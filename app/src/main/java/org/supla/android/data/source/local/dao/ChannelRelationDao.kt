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
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity

@Dao
interface ChannelRelationDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(channelRelation: ChannelRelationEntity): Completable

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = ${ProfileEntity.SUBQUERY_ACTIVE}")
  fun getForActiveProfile(): Single<List<ChannelRelationEntity>>

  @Query(
    """
      SELECT $ALL_COLUMNS FROM $TABLE_NAME 
        WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = :profileId 
        AND ${ChannelRelationEntity.COLUMN_PARENT_ID} = :parentId
    """
  )
  fun findChildren(profileId: Long, parentId: Int): Observable<List<ChannelRelationEntity>>

  @Query(
    """
      UPDATE $TABLE_NAME 
        SET ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1 
        WHERE ${ChannelRelationEntity.COLUMN_PROFILE_ID} = :profileId 
    """
  )
  fun markAsRemovable(profileId: Long): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE ${ChannelRelationEntity.COLUMN_DELETE_FLAG} = 1")
  fun cleanUnused(): Completable

  @Query(
    """
    SELECT 
      relation.${ChannelRelationEntity.COLUMN_PARENT_ID} relation_${ChannelRelationEntity.COLUMN_PARENT_ID},
      relation.${ChannelRelationEntity.COLUMN_CHANNEL_ID} relation_${ChannelRelationEntity.COLUMN_CHANNEL_ID},
      relation.${ChannelRelationEntity.COLUMN_CHANNEL_RELATION_TYPE} relation_${ChannelRelationEntity.COLUMN_CHANNEL_RELATION_TYPE},
      relation.${ChannelRelationEntity.COLUMN_DELETE_FLAG} relation_${ChannelRelationEntity.COLUMN_DELETE_FLAG},
      relation.${ChannelRelationEntity.COLUMN_PROFILE_ID} relation_${ChannelRelationEntity.COLUMN_PROFILE_ID},
      channel.${ChannelEntity.COLUMN_ID} channel_${ChannelEntity.COLUMN_ID}, 
      channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} channel_${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}, 
      channel.${ChannelEntity.COLUMN_DEVICE_ID} channel_${ChannelEntity.COLUMN_DEVICE_ID}, 
      channel.${ChannelEntity.COLUMN_CAPTION} channel_${ChannelEntity.COLUMN_CAPTION},
      channel.${ChannelEntity.COLUMN_TYPE} channel_${ChannelEntity.COLUMN_TYPE}, 
      channel.${ChannelEntity.COLUMN_FUNCTION} channel_${ChannelEntity.COLUMN_FUNCTION}, 
      channel.${ChannelEntity.COLUMN_VISIBLE} channel_${ChannelEntity.COLUMN_VISIBLE}, 
      channel.${ChannelEntity.COLUMN_LOCATION_ID} channel_${ChannelEntity.COLUMN_LOCATION_ID},
      channel.${ChannelEntity.COLUMN_ALT_ICON} channel_${ChannelEntity.COLUMN_ALT_ICON}, 
      channel.${ChannelEntity.COLUMN_USER_ICON} channel_${ChannelEntity.COLUMN_USER_ICON}, 
      channel.${ChannelEntity.COLUMN_MANUFACTURER_ID} channel_${ChannelEntity.COLUMN_MANUFACTURER_ID}, 
      channel.${ChannelEntity.COLUMN_PRODUCT_ID} channel_${ChannelEntity.COLUMN_PRODUCT_ID},
      channel.${ChannelEntity.COLUMN_FLAGS} channel_${ChannelEntity.COLUMN_FLAGS}, 
      channel.${ChannelEntity.COLUMN_PROTOCOL_VERSION} channel_${ChannelEntity.COLUMN_PROTOCOL_VERSION}, 
      channel.${ChannelEntity.COLUMN_POSITION} channel_${ChannelEntity.COLUMN_POSITION}, 
      channel.${ChannelEntity.COLUMN_PROFILE_ID} channel_${ChannelEntity.COLUMN_PROFILE_ID},
      value.${ChannelValueEntity.COLUMN_ID} value_${ChannelValueEntity.COLUMN_ID},
      value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} value_${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID},
      value.${ChannelValueEntity.COLUMN_ONLINE} value_${ChannelValueEntity.COLUMN_ONLINE},
      value.${ChannelValueEntity.COLUMN_SUB_VALUE_TYPE} value_${ChannelValueEntity.COLUMN_SUB_VALUE_TYPE},
      value.${ChannelValueEntity.COLUMN_SUB_VALUE} value_${ChannelValueEntity.COLUMN_SUB_VALUE},
      value.${ChannelValueEntity.COLUMN_VALUE} value_${ChannelValueEntity.COLUMN_VALUE},
      value.${ChannelValueEntity.COLUMN_PROFILE_ID} value_${ChannelValueEntity.COLUMN_PROFILE_ID},
      extended_value.${ChannelExtendedValueEntity.COLUMN_ID} extended_value_${ChannelExtendedValueEntity.COLUMN_ID},
      extended_value.${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID} extended_value_${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID},
      extended_value.${ChannelExtendedValueEntity.COLUMN_VALUE} extended_value_${ChannelExtendedValueEntity.COLUMN_VALUE},
      extended_value.${ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME} extended_value_${ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME},
      extended_value.${ChannelExtendedValueEntity.COLUMN_PROFILE_ID} extended_value_${ChannelExtendedValueEntity.COLUMN_PROFILE_ID},
      location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
      location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
      location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
      location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
      location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
      location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
      location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
      location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID},
      config.${ChannelConfigEntity.COLUMN_CHANNEL_ID} config_${ChannelConfigEntity.COLUMN_CHANNEL_ID},
      config.${ChannelConfigEntity.COLUMN_CONFIG_TYPE} config_${ChannelConfigEntity.COLUMN_CONFIG_TYPE},
      config.${ChannelConfigEntity.COLUMN_CONFIG} config_${ChannelConfigEntity.COLUMN_CONFIG},
      config.${ChannelConfigEntity.COLUMN_CONFIG_CRC32} config_${ChannelConfigEntity.COLUMN_CONFIG_CRC32},
      config.${ChannelConfigEntity.COLUMN_PROFILE_ID} config_${ChannelConfigEntity.COLUMN_PROFILE_ID}
    FROM $TABLE_NAME relation
    JOIN ${ChannelEntity.TABLE_NAME} channel
      ON relation.${ChannelRelationEntity.COLUMN_CHANNEL_ID} = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
        AND relation.${ChannelRelationEntity.COLUMN_PROFILE_ID} = channel.${ChannelEntity.COLUMN_PROFILE_ID}
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
    WHERE relation.${ChannelRelationEntity.COLUMN_PARENT_ID} = :parentRemoteId
      AND relation.${ChannelRelationEntity.COLUMN_PROFILE_ID} = ${ProfileEntity.SUBQUERY_ACTIVE}
  """
  )
  fun findChildrenFor(parentRemoteId: Int): Maybe<List<ChannelChildEntity>>
}
