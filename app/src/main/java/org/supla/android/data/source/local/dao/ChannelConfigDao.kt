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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.COLUMN_CONFIG_TYPE
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelConfigEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.ChannelConfigType

@Dao
interface ChannelConfigDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(entity: ChannelConfigEntity): Completable

  @Query(
    """
    SELECT $ALL_COLUMNS 
    FROM $TABLE_NAME 
    WHERE $COLUMN_PROFILE_ID = :profileId AND $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_CONFIG_TYPE = :type
    """
  )
  fun read(profileId: Long, channelId: Int, type: ChannelConfigType): Single<ChannelConfigEntity>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId AND $COLUMN_CHANNEL_ID = :channelId")
  fun delete(profileId: Long, channelId: Int): Completable

  @Query(
    """
    SELECT $ALL_COLUMNS 
    FROM $TABLE_NAME 
    WHERE $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND $COLUMN_CHANNEL_ID = :remoteId
  """
  )
  fun findForRemoteId(remoteId: Int): Maybe<ChannelConfigEntity>
}
