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
import androidx.room.Transaction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.ALL_COLUMNS_STRING
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ACTIVE
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.TABLE_NAME

@Dao
abstract class ProfileDao {

  @Query(
    """
    SELECT $ALL_COLUMNS_STRING 
    FROM $TABLE_NAME
    WHERE $COLUMN_ACTIVE = 1
  """
  )
  abstract fun findActiveProfile(): Single<ProfileEntity>

  @Query(
    """
    SELECT $ALL_COLUMNS_STRING 
    FROM $TABLE_NAME
    WHERE $COLUMN_ACTIVE = 1
  """
  )
  abstract suspend fun findActiveProfileKtx(): ProfileEntity

  @Query("SELECT $ALL_COLUMNS_STRING FROM $TABLE_NAME")
  abstract fun findAllProfiles(): Observable<List<ProfileEntity>>

  @Query(
    """
    SELECT $ALL_COLUMNS_STRING 
    FROM $TABLE_NAME
    WHERE $COLUMN_ID = :id
  """
  )
  abstract fun findProfile(id: Long): Single<ProfileEntity>

  fun activateProfile(id: Long): Completable = Completable.fromRunnable {
    activateProfileTransaction(id)
  }

  @Transaction
  protected open fun activateProfileTransaction(profileId: Long) {
    deactivateProfilesIntern()
    activateProfileIntern(profileId)
  }

  @Query("UPDATE $TABLE_NAME SET $COLUMN_ACTIVE = 0")
  protected abstract fun deactivateProfilesIntern()

  @Query("UPDATE $TABLE_NAME SET $COLUMN_ACTIVE = 1 WHERE $COLUMN_ID = :profileId")
  protected abstract fun activateProfileIntern(profileId: Long)

  @Query("SELECT ($COLUMN_ID) FROM $TABLE_NAME")
  abstract fun count(): Observable<Int>
}
