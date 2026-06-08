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
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_ACTION_ID
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_NAME
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_READ_ONLY
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_SUBJECT_ID
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_SUBJECT_TYPE
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.COLUMN_UUID
import org.supla.android.data.source.local.entity.NfcTagEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import org.supla.android.lib.actions.SubjectTypeValue

@Dao
interface NfcTagDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(entity: NfcTagEntity): Long

  @Query(
    """
      SELECT
        tag.$COLUMN_ID tag_$COLUMN_ID,
        tag.$COLUMN_UUID tag_$COLUMN_UUID,
        tag.$COLUMN_NAME tag_$COLUMN_NAME,
        tag.$COLUMN_PROFILE_ID tag_$COLUMN_PROFILE_ID,
        tag.$COLUMN_SUBJECT_TYPE tag_$COLUMN_SUBJECT_TYPE,
        tag.$COLUMN_SUBJECT_ID tag_$COLUMN_SUBJECT_ID,
        tag.$COLUMN_ACTION_ID tag_$COLUMN_ACTION_ID,
        tag.$COLUMN_READ_ONLY tag_$COLUMN_READ_ONLY,
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${SceneEntity.JOIN_COLUMNS},
        ${ProfileEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME tag
      LEFT JOIN ${ProfileEntity.TABLE_NAME} profile
        ON tag.$COLUMN_PROFILE_ID = profile.${ProfileEntity.COLUMN_ID}
      LEFT JOIN ${ChannelEntity.TABLE_NAME} channel
        ON tag.$COLUMN_SUBJECT_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
          AND tag.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = tag.$COLUMN_SUBJECT_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = tag.$COLUMN_PROFILE_ID
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
      LEFT JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON tag.$COLUMN_SUBJECT_ID = channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.GROUP}
          AND tag.$COLUMN_PROFILE_ID = channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${SceneEntity.TABLE_NAME} scene
        ON tag.$COLUMN_SUBJECT_ID = scene.${SceneEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
          AND tag.$COLUMN_PROFILE_ID = scene.${SceneEntity.COLUMN_PROFILE_ID}
      ORDER BY tag.$COLUMN_NAME COLLATE LOCALIZED
    """
  )
  suspend fun findAllWithDependencies(): List<NfcTagDataEntity>

  @Query(
    """
      SELECT
        ${NfcTagEntity.JOIN_COLUMNS},
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${SceneEntity.JOIN_COLUMNS},
        ${ProfileEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME tag
      LEFT JOIN ${ProfileEntity.TABLE_NAME} profile
        ON tag.$COLUMN_PROFILE_ID = profile.${ProfileEntity.COLUMN_ID}
      LEFT JOIN ${ChannelEntity.TABLE_NAME} channel
        ON tag.$COLUMN_SUBJECT_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
          AND tag.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = tag.$COLUMN_SUBJECT_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = tag.$COLUMN_PROFILE_ID
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
      LEFT JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON tag.$COLUMN_SUBJECT_ID = channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.GROUP}
          AND tag.$COLUMN_PROFILE_ID = channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${SceneEntity.TABLE_NAME} scene
        ON tag.$COLUMN_SUBJECT_ID = scene.${SceneEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
          AND tag.$COLUMN_PROFILE_ID = scene.${SceneEntity.COLUMN_PROFILE_ID}
      WHERE tag.$COLUMN_ID = :id
    """
  )
  suspend fun findByIdWithDependencies(id: Long): NfcTagDataEntity?

  @Query(
    """
      SELECT
        ${NfcTagEntity.JOIN_COLUMNS},
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelValueEntity.JOIN_COLUMNS},
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${SceneEntity.JOIN_COLUMNS},
        ${ProfileEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME tag
      LEFT JOIN ${ProfileEntity.TABLE_NAME} profile
        ON tag.$COLUMN_PROFILE_ID = profile.${ProfileEntity.COLUMN_ID}
      LEFT JOIN ${ChannelEntity.TABLE_NAME} channel
        ON tag.$COLUMN_SUBJECT_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
          AND tag.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = tag.$COLUMN_SUBJECT_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = tag.$COLUMN_PROFILE_ID
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
      LEFT JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON tag.$COLUMN_SUBJECT_ID = channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.GROUP}
          AND tag.$COLUMN_PROFILE_ID = channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${SceneEntity.TABLE_NAME} scene
        ON tag.$COLUMN_SUBJECT_ID = scene.${SceneEntity.COLUMN_REMOTE_ID}
          AND tag.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
          AND tag.$COLUMN_PROFILE_ID = scene.${SceneEntity.COLUMN_PROFILE_ID}
      WHERE tag.$COLUMN_UUID = :uuid
    """
  )
  suspend fun findByUuidWithDependencies(uuid: String): NfcTagDataEntity?

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  suspend fun delete(id: Long)

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  suspend fun findById(id: Long): NfcTagEntity?

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_UUID = :uuid")
  suspend fun findByUuid(uuid: String): NfcTagEntity?
}
