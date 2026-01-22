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

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME")
  suspend fun findAll(): List<NfcTagEntity>

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
        channel_group.${ChannelGroupEntity.COLUMN_ID} channel_group_${ChannelGroupEntity.COLUMN_ID},
        channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID} channel_group_${ChannelGroupEntity.COLUMN_REMOTE_ID},
        channel_group.${ChannelGroupEntity.COLUMN_CAPTION} channel_group_${ChannelGroupEntity.COLUMN_CAPTION},
        channel_group.${ChannelGroupEntity.COLUMN_ONLINE} channel_group_${ChannelGroupEntity.COLUMN_ONLINE},
        channel_group.${ChannelGroupEntity.COLUMN_FUNCTION} channel_group_${ChannelGroupEntity.COLUMN_FUNCTION},
        channel_group.${ChannelGroupEntity.COLUMN_VISIBLE} channel_group_${ChannelGroupEntity.COLUMN_VISIBLE},
        channel_group.${ChannelGroupEntity.COLUMN_LOCATION_ID} channel_group_${ChannelGroupEntity.COLUMN_LOCATION_ID},
        channel_group.${ChannelGroupEntity.COLUMN_ALT_ICON} channel_group_${ChannelGroupEntity.COLUMN_ALT_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_USER_ICON} channel_group_${ChannelGroupEntity.COLUMN_USER_ICON},
        channel_group.${ChannelGroupEntity.COLUMN_FLAGS} channel_group_${ChannelGroupEntity.COLUMN_FLAGS},
        channel_group.${ChannelGroupEntity.COLUMN_TOTAL_VALUE} channel_group_${ChannelGroupEntity.COLUMN_TOTAL_VALUE},
        channel_group.${ChannelGroupEntity.COLUMN_POSITION} channel_group_${ChannelGroupEntity.COLUMN_POSITION},
        channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID} channel_group_${ChannelGroupEntity.COLUMN_PROFILE_ID},
        scene.${SceneEntity.COLUMN_ID} scene_${SceneEntity.COLUMN_ID},
        scene.${SceneEntity.COLUMN_REMOTE_ID} scene_${SceneEntity.COLUMN_REMOTE_ID}, 
        scene.${SceneEntity.COLUMN_LOCATION_ID} scene_${SceneEntity.COLUMN_LOCATION_ID}, 
        scene.${SceneEntity.COLUMN_ALT_ICON} scene_${SceneEntity.COLUMN_ALT_ICON}, 
        scene.${SceneEntity.COLUMN_USER_ICON} scene_${SceneEntity.COLUMN_USER_ICON}, 
        scene.${SceneEntity.COLUMN_CAPTION} scene_${SceneEntity.COLUMN_CAPTION},
        scene.${SceneEntity.COLUMN_STARTED_AT} scene_${SceneEntity.COLUMN_STARTED_AT},
        scene.${SceneEntity.COLUMN_ESTIMATED_END_DATE} scene_${SceneEntity.COLUMN_ESTIMATED_END_DATE},
        scene.${SceneEntity.COLUMN_INITIATOR_ID} scene_${SceneEntity.COLUMN_INITIATOR_ID},
        scene.${SceneEntity.COLUMN_INITIATOR_NAME} scene_${SceneEntity.COLUMN_INITIATOR_NAME},
        scene.${SceneEntity.COLUMN_SORT_ORDER} scene_${SceneEntity.COLUMN_SORT_ORDER},
        scene.${SceneEntity.COLUMN_VISIBLE} scene_${SceneEntity.COLUMN_VISIBLE},
        scene.${SceneEntity.COLUMN_PROFILE_ID} scene_${SceneEntity.COLUMN_PROFILE_ID},
        profile.${ProfileEntity.COLUMN_ID} profile_${ProfileEntity.COLUMN_ID},
        profile.${ProfileEntity.COLUMN_NAME} profile_${ProfileEntity.COLUMN_NAME},
        profile.${ProfileEntity.COLUMN_EMAIL} profile_${ProfileEntity.COLUMN_EMAIL},
        profile.${ProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID} profile_${ProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID},
        profile.${ProfileEntity.COLUMN_SERVER_FOR_EMAIL} profile_${ProfileEntity.COLUMN_SERVER_FOR_EMAIL},
        profile.${ProfileEntity.COLUMN_SERVER_AUTO_DETECT} profile_${ProfileEntity.COLUMN_SERVER_AUTO_DETECT},
        profile.${ProfileEntity.COLUMN_EMAIL_AUTH} profile_${ProfileEntity.COLUMN_EMAIL_AUTH},
        profile.${ProfileEntity.COLUMN_ACCESS_ID} profile_${ProfileEntity.COLUMN_ACCESS_ID},
        profile.${ProfileEntity.COLUMN_ACCESS_ID_PASSWORD} profile_${ProfileEntity.COLUMN_ACCESS_ID_PASSWORD},
        profile.${ProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION} profile_${ProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION},
        profile.${ProfileEntity.COLUMN_ACTIVE} profile_${ProfileEntity.COLUMN_ACTIVE},
        profile.${ProfileEntity.COLUMN_ADVANCED_MODE} profile_${ProfileEntity.COLUMN_ADVANCED_MODE},
        profile.${ProfileEntity.COLUMN_POSITION} profile_${ProfileEntity.COLUMN_POSITION},
        profile.${ProfileEntity.COLUMN_GUID} profile_${ProfileEntity.COLUMN_GUID},
        profile.${ProfileEntity.COLUMN_AUTH_KEY} profile_${ProfileEntity.COLUMN_AUTH_KEY}
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

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  suspend fun delete(id: Long)

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  suspend fun findById(id: Long): NfcTagEntity?

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_UUID = :uuid")
  suspend fun findByUuid(uuid: String): NfcTagEntity?
}
