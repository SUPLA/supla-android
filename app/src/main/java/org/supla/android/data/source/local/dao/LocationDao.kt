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
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.LocationEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.LocationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.LocationEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.LocationEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.ProfileEntity

@Dao
interface LocationDao {

  @Query(
    """
    SELECT $ALL_COLUMNS 
    FROM $TABLE_NAME
    WHERE $COLUMN_REMOTE_ID = :remoteId
      AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
  """
  )
  fun findByRemoteId(remoteId: Int): Maybe<LocationEntity>

  @Update
  fun updateLocation(locationEntity: LocationEntity): Completable

  @Query("SELECT COUNT($COLUMN_PROFILE_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
