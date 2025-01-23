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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntity

@Dao
interface ChannelGroupRelationDao {

  @Query(
    """
      SELECT
        channel_group.${ChannelGroupEntity.COLUMN_ID} group_${ChannelGroupEntity.COLUMN_ID},
        channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} group_${ChannelGroupEntity.COLUMN_REMOTE_ID},
        channel_group.${ChannelGroupEntity.COLUMN_CAPTION} group_${ChannelGroupEntity.COLUMN_CAPTION},
        channel_group.${ChannelGroupEntity.COLUMN_ONLINE} group_${ChannelGroupEntity.COLUMN_ONLINE},
        channel_group.${ChannelGroupEntity.COLUMN_FUNCTION} group_${ChannelGroupEntity.COLUMN_FUNCTION},
        channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} group_${ChannelGroupEntity.COLUMN_VISIBLE},
        channel_group.${ChannelGroupEntity.COLUMN_LOCATION_ID} group_${ChannelGroupEntity.COLUMN_LOCATION_ID},
        channel_group.${ChannelGroupEntity.COLUMN_ALT_ICON} group_${ChannelGroupEntity.COLUMN_ALT_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_USER_ICON} group_${ChannelGroupEntity.COLUMN_USER_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_FLAGS} group_${ChannelGroupEntity.COLUMN_FLAGS},
        channel_group.${ChannelGroupEntity.COLUMN_TOTAL_VALUE} group_${ChannelGroupEntity.COLUMN_TOTAL_VALUE},
        channel_group.${ChannelGroupEntity.COLUMN_POSITION} group_${ChannelGroupEntity.COLUMN_POSITION},
        channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} group_${ChannelGroupEntity.COLUMN_PROFILE_ID},
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
        value.${ChannelValueEntity.COLUMN_PROFILE_ID} value_${ChannelValueEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME relation
      JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
          AND channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      JOIN ${ChannelEntity.TABLE_NAME} channel
        ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
          AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      WHERE relation.${ChannelGroupRelationEntity.COLUMN_VISIBLE} > 0
          AND channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} > 0  
          AND relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID} = ${ProfileEntity.SUBQUERY_ACTIVE}
      ORDER BY relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
    """
  )
  fun allVisibleRelations(): Single<List<ChannelGroupRelationDataEntity>>

  @Query(
    """
      SELECT
        channel_group.${ChannelGroupEntity.COLUMN_ID} group_${ChannelGroupEntity.COLUMN_ID},
        channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} group_${ChannelGroupEntity.COLUMN_REMOTE_ID},
        channel_group.${ChannelGroupEntity.COLUMN_CAPTION} group_${ChannelGroupEntity.COLUMN_CAPTION},
        channel_group.${ChannelGroupEntity.COLUMN_ONLINE} group_${ChannelGroupEntity.COLUMN_ONLINE},
        channel_group.${ChannelGroupEntity.COLUMN_FUNCTION} group_${ChannelGroupEntity.COLUMN_FUNCTION},
        channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} group_${ChannelGroupEntity.COLUMN_VISIBLE},
        channel_group.${ChannelGroupEntity.COLUMN_LOCATION_ID} group_${ChannelGroupEntity.COLUMN_LOCATION_ID},
        channel_group.${ChannelGroupEntity.COLUMN_ALT_ICON} group_${ChannelGroupEntity.COLUMN_ALT_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_USER_ICON} group_${ChannelGroupEntity.COLUMN_USER_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_FLAGS} group_${ChannelGroupEntity.COLUMN_FLAGS},
        channel_group.${ChannelGroupEntity.COLUMN_TOTAL_VALUE} group_${ChannelGroupEntity.COLUMN_TOTAL_VALUE},
        channel_group.${ChannelGroupEntity.COLUMN_POSITION} group_${ChannelGroupEntity.COLUMN_POSITION},
        channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} group_${ChannelGroupEntity.COLUMN_PROFILE_ID},
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
        value.${ChannelValueEntity.COLUMN_PROFILE_ID} value_${ChannelValueEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME relation
      JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
          AND channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      JOIN ${ChannelEntity.TABLE_NAME} channel
        ON channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
          AND channel.${ChannelEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID}
      WHERE relation.${ChannelGroupRelationEntity.COLUMN_VISIBLE} > 0
          AND channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} > 0  
          AND relation.${ChannelGroupRelationEntity.COLUMN_PROFILE_ID} = ${ProfileEntity.SUBQUERY_ACTIVE}
          AND relation.${ChannelGroupRelationEntity.COLUMN_GROUP_ID} = :remoteId
    """
  )
  fun findGroupRelations(remoteId: Int): Single<List<ChannelGroupRelationDataEntity>>

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
