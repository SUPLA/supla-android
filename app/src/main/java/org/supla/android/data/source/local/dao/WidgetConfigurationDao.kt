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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_ACTION
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_GLANCE_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_SUBJECT_ID
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.COLUMN_SUBJECT_TYPE
import org.supla.android.data.source.local.entity.WidgetConfigurationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.complex.WidgetConfigurationDataEntity
import org.supla.android.lib.actions.SubjectTypeValue

@Dao
interface WidgetConfigurationDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(configuration: WidgetConfigurationEntity): Completable

  @Query(
    """
      SELECT $ALL_COLUMNS
      FROM $TABLE_NAME
      WHERE $COLUMN_GLANCE_ID = :glanceId
    """
  )
  fun findEntityBy(glanceId: String): Single<WidgetConfigurationEntity>

  @Query(
    """
      SELECT
        configuration.$COLUMN_ID configuration_$COLUMN_ID,
        configuration.$COLUMN_SUBJECT_ID configuration_$COLUMN_SUBJECT_ID,
        configuration.$COLUMN_SUBJECT_TYPE configuration_$COLUMN_SUBJECT_TYPE,
        configuration.$COLUMN_CAPTION configuration_$COLUMN_CAPTION,
        configuration.$COLUMN_ACTION configuration_$COLUMN_ACTION,
        configuration.$COLUMN_PROFILE_ID configuration_$COLUMN_PROFILE_ID,
        configuration.$COLUMN_GLANCE_ID configuration_$COLUMN_GLANCE_ID,
        ${ChannelEntity.JOIN_COLUMNS},
        ${ChannelGroupEntity.JOIN_COLUMNS},
        ${SceneEntity.JOIN_COLUMNS},
        ${ProfileEntity.JOIN_COLUMNS}
      FROM $TABLE_NAME configuration
      JOIN ${ProfileEntity.TABLE_NAME} profile
        ON configuration.$COLUMN_PROFILE_ID = profile.${ProfileEntity.COLUMN_ID}
      LEFT JOIN ${ChannelEntity.TABLE_NAME} channel
        ON configuration.$COLUMN_SUBJECT_ID = channel.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID}
          AND configuration.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
          AND configuration.$COLUMN_PROFILE_ID = channel.${ChannelEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${ChannelValueEntity.TABLE_NAME} value
        ON value.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = configuration.$COLUMN_SUBJECT_ID
          AND value.${ChannelValueEntity.COLUMN_PROFILE_ID} = configuration.$COLUMN_PROFILE_ID
          AND configuration.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.CHANNEL}
      LEFT JOIN ${ChannelGroupEntity.TABLE_NAME} channel_group
        ON configuration.$COLUMN_SUBJECT_ID = channel_group.${ChannelGroupEntity.COLUMN_REMOTE_ID}
          AND configuration.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.GROUP}
          AND configuration.$COLUMN_PROFILE_ID = channel_group.${ChannelGroupEntity.COLUMN_PROFILE_ID}
      LEFT JOIN ${SceneEntity.TABLE_NAME} scene
        ON configuration.$COLUMN_SUBJECT_ID = scene.${SceneEntity.COLUMN_REMOTE_ID}
          AND configuration.$COLUMN_SUBJECT_TYPE = ${SubjectTypeValue.SCENE}
          AND configuration.$COLUMN_PROFILE_ID = scene.${SceneEntity.COLUMN_PROFILE_ID}
      WHERE configuration.$COLUMN_GLANCE_ID = :glanceId
    """
  )
  suspend fun findBy(glanceId: String): WidgetConfigurationDataEntity?

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_GLANCE_ID = :glanceId")
  suspend fun deleteBy(glanceId: String)
}
