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
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_CHANNEL_REMOTE_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_POSITION
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.ChannelEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.ONLINE_BUT_NOT_AVAILABLE
import org.supla.core.shared.data.model.general.SuplaFunction

@Dao
interface ChannelDao {

  @Update
  fun update(entity: ChannelEntity): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(entity: ChannelEntity): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_CHANNEL_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  suspend fun deleteKtx(remoteId: Int, profileId: Long)

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
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
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
      location.${LocationEntity.COLUMN_CAPTION} COLLATE UNICODE,
      channel.${COLUMN_POSITION},
      channel.${ChannelEntity.COLUMN_FUNCTION} DESC,
      channel.$COLUMN_CAPTION COLLATE LOCALIZED
  """
  )
  fun findList(): Observable<List<ChannelDataEntity>>

  @Query(
    """
    SELECT 
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
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
    WHERE channel.${ChannelEntity.COLUMN_FUNCTION} = :function
      AND channel.$COLUMN_PROFILE_ID = :profileId
      AND channel.$COLUMN_VISIBLE > 0
    ORDER BY
      location.${LocationEntity.COLUMN_SORT_ORDER},
      location.${LocationEntity.COLUMN_CAPTION} COLLATE UNICODE,
      channel.${COLUMN_POSITION},
      channel.${ChannelEntity.COLUMN_FUNCTION} DESC,
      channel.$COLUMN_CAPTION COLLATE LOCALIZED
  """
  )
  suspend fun findChannelsBy(profileId: Long, function: SuplaFunction): List<ChannelDataEntity>

  @Query(
    """
    SELECT 
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
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
      AND (value.${ChannelValueEntity.COLUMN_LAST_ONLINE_STATE} IS NULL OR value.${ChannelValueEntity.COLUMN_LAST_ONLINE_STATE} <> ${ONLINE_BUT_NOT_AVAILABLE})
    ORDER BY
      location.${LocationEntity.COLUMN_SORT_ORDER},
      location.${LocationEntity.COLUMN_CAPTION} COLLATE UNICODE,
      channel.${COLUMN_POSITION},
      channel.${ChannelEntity.COLUMN_FUNCTION} DESC,
      channel.$COLUMN_CAPTION COLLATE LOCALIZED
  """
  )
  fun findListWithoutUnavailable(): Observable<List<ChannelDataEntity>>

  @Query(
    """
    SELECT 
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
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
      AND channel.$COLUMN_PROFILE_ID = :profileId
    ORDER BY
      location.${LocationEntity.COLUMN_SORT_ORDER},
      location.${LocationEntity.COLUMN_CAPTION} COLLATE UNICODE,
      channel.${COLUMN_POSITION},
      channel.${ChannelEntity.COLUMN_FUNCTION} DESC,
      channel.$COLUMN_CAPTION COLLATE LOCALIZED
  """
  )
  fun findList(profileId: Long): Observable<List<ChannelDataEntity>>

  @Query(
    """
    SELECT 
      ${ChannelEntity.JOIN_COLUMNS},
      ${ChannelValueEntity.JOIN_COLUMNS},
      ${ChannelExtendedValueEntity.JOIN_COLUMNS},
      ${LocationEntity.JOIN_COLUMNS},
      ${ChannelConfigEntity.JOIN_COLUMNS},
      ${ChannelStateEntity.JOIN_COLUMNS}
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
  fun findChannelDataEntity(channelRemoteId: Int): Observable<ChannelDataEntity>

  @Query(
    """
    SELECT MAX($COLUMN_POSITION)
    FROM $TABLE_NAME
    WHERE $COLUMN_LOCATION_ID = :locationRemoteId
      AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
    GROUP BY $COLUMN_LOCATION_ID
  """
  )
  fun findMaxPositionInLocation(locationRemoteId: Int): Single<Int>

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

  @Query(
    """
      UPDATE $TABLE_NAME SET $COLUMN_CAPTION = :caption 
      WHERE $COLUMN_CHANNEL_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId
    """
  )
  fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  fun deleteByProfile(profileId: Long): Completable

  @Query(
    """
      SELECT
        $COLUMN_ID, 
        $COLUMN_CHANNEL_REMOTE_ID, 
        ${ChannelEntity.COLUMN_DEVICE_ID}, 
        $COLUMN_CAPTION,
        ${ChannelEntity.COLUMN_TYPE}, 
        ${ChannelEntity.COLUMN_FUNCTION}, 
        $COLUMN_VISIBLE, 
        $COLUMN_LOCATION_ID,
        ${ChannelEntity.COLUMN_ALT_ICON}, 
        ${ChannelEntity.COLUMN_USER_ICON}, 
        ${ChannelEntity.COLUMN_MANUFACTURER_ID}, 
        ${ChannelEntity.COLUMN_PRODUCT_ID},
        ${ChannelEntity.COLUMN_FLAGS}, 
        ${ChannelEntity.COLUMN_PROTOCOL_VERSION}, 
        ${ChannelEntity.COLUMN_POSITION}, 
        $COLUMN_PROFILE_ID
      FROM $TABLE_NAME
      WHERE
        $COLUMN_VISIBLE == 0 AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
    """
  )
  suspend fun findHiddenChannels(): List<ChannelEntity>
}
