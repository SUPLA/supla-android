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
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.SUBQUERY_ACTIVE

@Dao
interface ChannelStateDao {
  @Query(
    """
      SELECT $ALL_COLUMNS FROM $TABLE_NAME
      WHERE ${ChannelStateEntity.COLUMN_CHANNEL_ID} = :channelId
        AND $COLUMN_PROFILE_ID = $SUBQUERY_ACTIVE
    """
  )
  fun getState(channelId: Int): Maybe<ChannelStateEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrUpdate(channelState: ChannelStateEntity): Completable

  @Query("SELECT COUNT($COLUMN_PROFILE_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
