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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_ACTION
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_ORDER
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_SUBJECT_ID
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_SUBJECT_TYPE
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.complex.AndroidAutoDataEntity
import org.supla.android.lib.actions.SubjectTypeValue

@Dao
abstract class AndroidAutoItemDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insert(item: AndroidAutoItemEntity): Completable

  @Query("SELECT MAX($COLUMN_ORDER) FROM $TABLE_NAME")
  abstract fun lastOrderNo(): Single<Int>

  @Query(
    """
      SELECT $ALL_COLUMNS
      FROM $TABLE_NAME
      WHERE $COLUMN_ID == :id
    """
  )
  abstract fun findById(id: Long): Observable<AndroidAutoItemEntity>

  @Query(
    """
      SELECT
        item.$COLUMN_ID item_$COLUMN_ID,
        item.$COLUMN_SUBJECT_ID item_$COLUMN_SUBJECT_ID,
        item.$COLUMN_SUBJECT_TYPE item_$COLUMN_SUBJECT_TYPE,
        item.$COLUMN_CAPTION item_$COLUMN_CAPTION,
        item.$COLUMN_ACTION item_$COLUMN_ACTION,
        item.$COLUMN_PROFILE_ID item_$COLUMN_PROFILE_ID,
        item.$COLUMN_ORDER item_$COLUMN_ORDER,
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${SceneEntity.JOIN_COLUMNS},
        ${ProfileEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME item
      JOIN ${ProfileEntity.TABLE_NAME} profile
        ON item.$COLUMN_PROFILE_ID = profile.${ProfileEntity.COLUMN_ID}
      LEFT JOIN ${ChannelEntity.TABLE_NAME} channel
        ON item.$COLUMN_SUBJECT_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
          AND item.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
          AND item.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = item.$COLUMN_SUBJECT_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = item.$COLUMN_PROFILE_ID
          AND item.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
      LEFT JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON item.$COLUMN_SUBJECT_ID = channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID}
          AND item.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.GROUP}
          AND item.$COLUMN_PROFILE_ID = channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${SceneEntity.TABLE_NAME} scene
        ON item.$COLUMN_SUBJECT_ID = scene.${SceneEntity.COLUMN_REMOTE_ID}
          AND item.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
          AND item.$COLUMN_PROFILE_ID = scene.${SceneEntity.COLUMN_PROFILE_ID}
      ORDER BY item.$COLUMN_ORDER, item.$COLUMN_ID
    """
  )
  abstract fun findAll(): Observable<List<AndroidAutoDataEntity>>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  abstract fun delete(id: Long): Completable

  @Query("UPDATE $TABLE_NAME SET $COLUMN_ORDER = :order WHERE $COLUMN_ID = :id")
  abstract fun setOrder(id: Long, order: Int)

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  abstract fun deleteByProfile(profileId: Long): Completable

  @Transaction
  open fun setOrder(orderedIds: List<Long>) {
    var orderNo = 1
    orderedIds.forEach { setOrder(it, orderNo++) }
  }

  fun setItemsOrder(orderedIds: List<Long>): Completable =
    Completable.fromRunnable { setOrder(orderedIds) }

  @Query(
    """
    DELETE FROM $TABLE_NAME 
    WHERE $COLUMN_SUBJECT_ID = :remoteId 
      AND $COLUMN_PROFILE_ID = :profileId 
      AND $COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
  """
  )
  abstract suspend fun deleteChannelRelated(remoteId: Int, profileId: Long)

  @Query(
    """
    DELETE FROM $TABLE_NAME 
    WHERE $COLUMN_SUBJECT_ID = :remoteId 
      AND $COLUMN_PROFILE_ID = :profileId 
      AND $COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
  """
  )
  abstract suspend fun deleteSceneRelated(remoteId: Int, profileId: Long)
}
