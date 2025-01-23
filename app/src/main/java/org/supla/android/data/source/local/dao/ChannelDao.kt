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
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_CHANNEL_REMOTE_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity

@Dao
interface ChannelDao {

  @Update
  fun update(entity: ChannelEntity): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(entity: ChannelEntity): Completable

  @Query(
    """
    SELECT $ALL_COLUMNS FROM channel
    WHERE
      $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND channelid = :remoteId
    """
  )
  fun findByRemoteId(remoteId: Int): Maybe<ChannelEntity>

  @Query(
    """
    SELECT $ALL_COLUMNS FROM channel
    WHERE
      $COLUMN_PROFILE_ID = :profileId
      AND $COLUMN_CHANNEL_REMOTE_ID = :remoteId
    """
  )
  fun findByRemoteId(profileId: Long, remoteId: Int): Maybe<ChannelEntity>

  @Query(
    """
    SELECT 
      channel.$COLUMN_ID channel_$COLUMN_ID, 
      channel.$COLUMN_CHANNEL_REMOTE_ID channel_$COLUMN_CHANNEL_REMOTE_ID, 
      channel.${ChannelEntity.COLUMN_DEVICE_ID} channel_${ChannelEntity.COLUMN_DEVICE_ID}, 
      channel.${ChannelEntity.COLUMN_CAPTION} channel_${ChannelEntity.COLUMN_CAPTION},
      channel.${ChannelEntity.COLUMN_TYPE} channel_${ChannelEntity.COLUMN_TYPE}, 
      channel.${ChannelEntity.COLUMN_FUNCTION} channel_${ChannelEntity.COLUMN_FUNCTION}, 
      channel.$COLUMN_VISIBLE channel_$COLUMN_VISIBLE, 
      channel.$COLUMN_LOCATION_ID channel_$COLUMN_LOCATION_ID,
      channel.${ChannelEntity.COLUMN_ALT_ICON} channel_${ChannelEntity.COLUMN_ALT_ICON}, 
      channel.${ChannelEntity.COLUMN_USER_ICON} channel_${ChannelEntity.COLUMN_USER_ICON}, 
      channel.${ChannelEntity.COLUMN_MANUFACTURER_ID} channel_${ChannelEntity.COLUMN_MANUFACTURER_ID}, 
      channel.${ChannelEntity.COLUMN_PRODUCT_ID} channel_${ChannelEntity.COLUMN_PRODUCT_ID},
      channel.${ChannelEntity.COLUMN_FLAGS} channel_${ChannelEntity.COLUMN_FLAGS}, 
      channel.${ChannelEntity.COLUMN_PROTOCOL_VERSION} channel_${ChannelEntity.COLUMN_PROTOCOL_VERSION}, 
      channel.${ChannelEntity.COLUMN_POSITION} channel_${ChannelEntity.COLUMN_POSITION}, 
      channel.$COLUMN_PROFILE_ID channel_$COLUMN_PROFILE_ID,
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
      config.${ChannelConfigEntity.COLUMN_PROFILE_ID} config_${ChannelConfigEntity.COLUMN_PROFILE_ID},
      state.${ChannelStateEntity.COLUMN_BATTERY_HEALTH} state_${ChannelStateEntity.COLUMN_BATTERY_HEALTH},
      state.${ChannelStateEntity.COLUMN_BATTERY_LEVEL} state_${ChannelStateEntity.COLUMN_BATTERY_LEVEL},
      state.${ChannelStateEntity.COLUMN_BATTERY_POWERED} state_${ChannelStateEntity.COLUMN_BATTERY_POWERED},
      state.${ChannelStateEntity.COLUMN_BRIDGE_NODE_ONLINE} state_${ChannelStateEntity.COLUMN_BRIDGE_NODE_ONLINE},
      state.${ChannelStateEntity.COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH} state_${ChannelStateEntity.COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH},
      state.${ChannelStateEntity.COLUMN_CONNECTION_UPTIME} state_${ChannelStateEntity.COLUMN_CONNECTION_UPTIME},
      state.${ChannelStateEntity.COLUMN_IP_V4} state_${ChannelStateEntity.COLUMN_IP_V4},
      state.${ChannelStateEntity.COLUMN_LAST_CONNECTION_RESET_CAUSE} state_${ChannelStateEntity.COLUMN_LAST_CONNECTION_RESET_CAUSE},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_OPERATING_TIME} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_OPERATING_TIME},
      state.${ChannelStateEntity.COLUMN_MAC_ADDRESS} state_${ChannelStateEntity.COLUMN_MAC_ADDRESS},
      state.${ChannelStateEntity.COLUMN_UPTIME} state_${ChannelStateEntity.COLUMN_UPTIME},
      state.${ChannelStateEntity.COLUMN_WIFI_RSSI} state_${ChannelStateEntity.COLUMN_WIFI_RSSI},
      state.${ChannelStateEntity.COLUMN_WIFI_SIGNAL_STRENGTH} state_${ChannelStateEntity.COLUMN_WIFI_SIGNAL_STRENGTH},
      state.${ChannelStateEntity.COLUMN_CHANNEL_ID} state_${ChannelStateEntity.COLUMN_CHANNEL_ID},
      state.${ChannelStateEntity.COLUMN_PROFILE_ID} state_${ChannelStateEntity.COLUMN_PROFILE_ID}
    FROM $TABLE_NAME channel
    JOIN ${ChannelValueEntity.TABLE_NAME} value
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID}
        AND channel.$COLUMN_PROFILE_ID = value.${ChannelValueEntity.COLUMN_PROFILE_ID}
    JOIN ${LocationEntity.TABLE_NAME} location
      ON channel.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
        AND channel.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelConfigEntity.TABLE_NAME} config
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = config.${ChannelConfigEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = config.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelExtendedValueEntity.TABLE_NAME} extended_value
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = extended_value.${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = extended_value.${ChannelExtendedValueEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelStateEntity.TABLE_NAME} state
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = state.${ChannelStateEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = state.${ChannelStateEntity.COLUMN_PROFILE_ID}
    WHERE channel.${ChannelEntity.COLUMN_FUNCTION} <> 0
      AND channel.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND channel.$COLUMN_VISIBLE > 0
    ORDER BY
      location.${LocationEntity.COLUMN_SORT_ORDER},
      location.${LocationEntity.COLUMN_CAPTION} COLLATE LOCALIZED,
      channel.${ChannelEntity.COLUMN_POSITION},
      channel.${ChannelEntity.COLUMN_FUNCTION} DESC,
      channel.${ChannelEntity.COLUMN_CAPTION} COLLATE LOCALIZED
  """
  )
  fun findList(): Observable<List<ChannelDataEntity>>

  @Query(
    """
    SELECT 
      channel.$COLUMN_ID channel_$COLUMN_ID, 
      channel.$COLUMN_CHANNEL_REMOTE_ID channel_$COLUMN_CHANNEL_REMOTE_ID, 
      channel.${ChannelEntity.COLUMN_DEVICE_ID} channel_${ChannelEntity.COLUMN_DEVICE_ID}, 
      channel.${ChannelEntity.COLUMN_CAPTION} channel_${ChannelEntity.COLUMN_CAPTION},
      channel.${ChannelEntity.COLUMN_TYPE} channel_${ChannelEntity.COLUMN_TYPE}, 
      channel.${ChannelEntity.COLUMN_FUNCTION} channel_${ChannelEntity.COLUMN_FUNCTION}, 
      channel.$COLUMN_VISIBLE channel_$COLUMN_VISIBLE, 
      channel.$COLUMN_LOCATION_ID channel_$COLUMN_LOCATION_ID,
      channel.${ChannelEntity.COLUMN_ALT_ICON} channel_${ChannelEntity.COLUMN_ALT_ICON}, 
      channel.${ChannelEntity.COLUMN_USER_ICON} channel_${ChannelEntity.COLUMN_USER_ICON}, 
      channel.${ChannelEntity.COLUMN_MANUFACTURER_ID} channel_${ChannelEntity.COLUMN_MANUFACTURER_ID}, 
      channel.${ChannelEntity.COLUMN_PRODUCT_ID} channel_${ChannelEntity.COLUMN_PRODUCT_ID},
      channel.${ChannelEntity.COLUMN_FLAGS} channel_${ChannelEntity.COLUMN_FLAGS}, 
      channel.${ChannelEntity.COLUMN_PROTOCOL_VERSION} channel_${ChannelEntity.COLUMN_PROTOCOL_VERSION}, 
      channel.${ChannelEntity.COLUMN_POSITION} channel_${ChannelEntity.COLUMN_POSITION}, 
      channel.$COLUMN_PROFILE_ID channel_$COLUMN_PROFILE_ID,
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
      config.${ChannelConfigEntity.COLUMN_PROFILE_ID} config_${ChannelConfigEntity.COLUMN_PROFILE_ID},
      state.${ChannelStateEntity.COLUMN_BATTERY_HEALTH} state_${ChannelStateEntity.COLUMN_BATTERY_HEALTH},
      state.${ChannelStateEntity.COLUMN_BATTERY_LEVEL} state_${ChannelStateEntity.COLUMN_BATTERY_LEVEL},
      state.${ChannelStateEntity.COLUMN_BATTERY_POWERED} state_${ChannelStateEntity.COLUMN_BATTERY_POWERED},
      state.${ChannelStateEntity.COLUMN_BRIDGE_NODE_ONLINE} state_${ChannelStateEntity.COLUMN_BRIDGE_NODE_ONLINE},
      state.${ChannelStateEntity.COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH} state_${ChannelStateEntity.COLUMN_BRIDGE_NODE_SIGNAL_STRENGTH},
      state.${ChannelStateEntity.COLUMN_CONNECTION_UPTIME} state_${ChannelStateEntity.COLUMN_CONNECTION_UPTIME},
      state.${ChannelStateEntity.COLUMN_IP_V4} state_${ChannelStateEntity.COLUMN_IP_V4},
      state.${ChannelStateEntity.COLUMN_LAST_CONNECTION_RESET_CAUSE} state_${ChannelStateEntity.COLUMN_LAST_CONNECTION_RESET_CAUSE},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_LIFESPAN_LEFT},
      state.${ChannelStateEntity.COLUMN_LIGHT_SOURCE_OPERATING_TIME} state_${ChannelStateEntity.COLUMN_LIGHT_SOURCE_OPERATING_TIME},
      state.${ChannelStateEntity.COLUMN_MAC_ADDRESS} state_${ChannelStateEntity.COLUMN_MAC_ADDRESS},
      state.${ChannelStateEntity.COLUMN_UPTIME} state_${ChannelStateEntity.COLUMN_UPTIME},
      state.${ChannelStateEntity.COLUMN_WIFI_RSSI} state_${ChannelStateEntity.COLUMN_WIFI_RSSI},
      state.${ChannelStateEntity.COLUMN_WIFI_SIGNAL_STRENGTH} state_${ChannelStateEntity.COLUMN_WIFI_SIGNAL_STRENGTH},
      state.${ChannelStateEntity.COLUMN_CHANNEL_ID} state_${ChannelStateEntity.COLUMN_CHANNEL_ID},
      state.${ChannelStateEntity.COLUMN_PROFILE_ID} state_${ChannelStateEntity.COLUMN_PROFILE_ID}
    FROM $TABLE_NAME channel
    JOIN ${ChannelValueEntity.TABLE_NAME} value
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID}
        AND channel.$COLUMN_PROFILE_ID = value.${ChannelValueEntity.COLUMN_PROFILE_ID}
    JOIN ${LocationEntity.TABLE_NAME} location
      ON channel.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
        AND channel.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelConfigEntity.TABLE_NAME} config
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = config.${ChannelConfigEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = config.${LocationEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelExtendedValueEntity.TABLE_NAME} extended_value
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = extended_value.${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = extended_value.${ChannelExtendedValueEntity.COLUMN_PROFILE_ID}
    LEFT JOIN ${ChannelStateEntity.TABLE_NAME} state
      ON channel.$COLUMN_CHANNEL_REMOTE_ID = state.${ChannelStateEntity.COLUMN_CHANNEL_ID}
        AND channel.$COLUMN_PROFILE_ID = state.${ChannelStateEntity.COLUMN_PROFILE_ID}
    WHERE channel.$COLUMN_CHANNEL_REMOTE_ID = :channelRemoteId
      AND channel.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
  """
  )
  fun findChannelDataEntity(channelRemoteId: Int): Maybe<ChannelDataEntity>

  @Query(
    """
    SELECT COUNT($COLUMN_ID)
    FROM $TABLE_NAME
    WHERE $COLUMN_LOCATION_ID = :locationRemoteId
      AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND $COLUMN_VISIBLE > 0
  """
  )
  fun findChannelCountInLocation(locationRemoteId: Int): Single<Int>

  @Query(
    """
    SELECT COUNT($COLUMN_ID)
    FROM $TABLE_NAME
    WHERE ${ChannelEntity.COLUMN_FUNCTION} <> 0
      AND $COLUMN_PROFILE_ID = :profileId
      AND $COLUMN_VISIBLE > 0
  """
  )
  fun findChannelsCount(profileId: Long): Single<Int>

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
