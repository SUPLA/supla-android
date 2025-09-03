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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.SceneEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_ALT_ICON
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_CAPTION
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_ESTIMATED_END_DATE
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_INITIATOR_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_INITIATOR_NAME
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_LOCATION_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_REMOTE_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_SORT_ORDER
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_STARTED_AT
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_USER_ICON
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.SceneEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.entity.complex.SceneDataEntity

@Dao
interface SceneDao {

  @Query(
    """
    SELECT $ALL_COLUMNS FROM $TABLE_NAME
    WHERE
      $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}
      AND $COLUMN_REMOTE_ID = :remoteId
    """
  )
  fun findByRemoteId(remoteId: Int): Maybe<SceneEntity>

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE $COLUMN_VISIBLE = 0 AND $COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE}")
  suspend fun findHiddenScenes(): List<SceneEntity>

  @Query(
    """
      SELECT
        scene.$COLUMN_ID scene_$COLUMN_ID,
        scene.$COLUMN_REMOTE_ID scene_$COLUMN_REMOTE_ID, 
        scene.$COLUMN_LOCATION_ID scene_$COLUMN_LOCATION_ID, 
        scene.$COLUMN_ALT_ICON scene_$COLUMN_ALT_ICON, 
        scene.$COLUMN_USER_ICON scene_$COLUMN_USER_ICON, 
        scene.$COLUMN_CAPTION scene_$COLUMN_CAPTION,
        scene.$COLUMN_STARTED_AT scene_$COLUMN_STARTED_AT,
        scene.$COLUMN_ESTIMATED_END_DATE scene_$COLUMN_ESTIMATED_END_DATE,
        scene.$COLUMN_INITIATOR_ID scene_$COLUMN_INITIATOR_ID,
        scene.$COLUMN_INITIATOR_NAME scene_$COLUMN_INITIATOR_NAME,
        scene.$COLUMN_SORT_ORDER scene_$COLUMN_SORT_ORDER,
        scene.$COLUMN_VISIBLE scene_$COLUMN_VISIBLE,
        scene.$COLUMN_PROFILE_ID scene_$COLUMN_PROFILE_ID,
        location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
        location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
        location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
        location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
        location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
        location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
        location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME scene
      JOIN ${LocationEntity.TABLE_NAME} location
        ON scene.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
          AND scene.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
      WHERE scene.$COLUMN_PROFILE_ID = ${ProfileEntity.SUBQUERY_ACTIVE} AND
        scene.$COLUMN_VISIBLE > 0
      ORDER BY location.${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_CAPTION} COLLATE LOCALIZED,
        scene.$COLUMN_SORT_ORDER,
        scene.$COLUMN_CAPTION COLLATE LOCALIZED,
        scene.$COLUMN_REMOTE_ID
    """
  )
  fun findList(): Single<List<SceneDataEntity>>

  @Query(
    """
      SELECT
        scene.$COLUMN_ID scene_$COLUMN_ID,
        scene.$COLUMN_REMOTE_ID scene_$COLUMN_REMOTE_ID, 
        scene.$COLUMN_LOCATION_ID scene_$COLUMN_LOCATION_ID, 
        scene.$COLUMN_ALT_ICON scene_$COLUMN_ALT_ICON, 
        scene.$COLUMN_USER_ICON scene_$COLUMN_USER_ICON, 
        scene.$COLUMN_CAPTION scene_$COLUMN_CAPTION,
        scene.$COLUMN_STARTED_AT scene_$COLUMN_STARTED_AT,
        scene.$COLUMN_ESTIMATED_END_DATE scene_$COLUMN_ESTIMATED_END_DATE,
        scene.$COLUMN_INITIATOR_ID scene_$COLUMN_INITIATOR_ID,
        scene.$COLUMN_INITIATOR_NAME scene_$COLUMN_INITIATOR_NAME,
        scene.$COLUMN_SORT_ORDER scene_$COLUMN_SORT_ORDER,
        scene.$COLUMN_VISIBLE scene_$COLUMN_VISIBLE,
        scene.$COLUMN_PROFILE_ID scene_$COLUMN_PROFILE_ID,
        location.${LocationEntity.COLUMN_ID} location_${LocationEntity.COLUMN_ID},
        location.${LocationEntity.COLUMN_REMOTE_ID} location_${LocationEntity.COLUMN_REMOTE_ID},
        location.${LocationEntity.COLUMN_CAPTION} location_${LocationEntity.COLUMN_CAPTION},
        location.${LocationEntity.COLUMN_VISIBLE} location_${LocationEntity.COLUMN_VISIBLE},
        location.${LocationEntity.COLUMN_COLLAPSED} location_${LocationEntity.COLUMN_COLLAPSED},
        location.${LocationEntity.COLUMN_SORTING} location_${LocationEntity.COLUMN_SORTING},
        location.${LocationEntity.COLUMN_SORT_ORDER} location_${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_PROFILE_ID} location_${LocationEntity.COLUMN_PROFILE_ID}
      FROM $TABLE_NAME scene
      JOIN ${LocationEntity.TABLE_NAME} location
        ON scene.$COLUMN_LOCATION_ID = location.${LocationEntity.COLUMN_REMOTE_ID}
          AND scene.$COLUMN_PROFILE_ID = location.${LocationEntity.COLUMN_PROFILE_ID}
      WHERE scene.$COLUMN_PROFILE_ID = :profileId AND
        scene.$COLUMN_VISIBLE > 0
      ORDER BY location.${LocationEntity.COLUMN_SORT_ORDER},
        location.${LocationEntity.COLUMN_CAPTION} COLLATE LOCALIZED,
        scene.$COLUMN_SORT_ORDER,
        scene.$COLUMN_CAPTION COLLATE LOCALIZED,
        scene.$COLUMN_REMOTE_ID
    """
  )
  fun findProfileScenes(profileId: Long): Single<List<SceneDataEntity>>

  @Update
  fun update(scenes: List<SceneEntity>): Completable

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>

  @Query("UPDATE $TABLE_NAME SET $COLUMN_CAPTION = :caption WHERE $COLUMN_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_REMOTE_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  suspend fun deleteScene(profileId: Long, remoteId: Int)

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_PROFILE_ID = :profileId")
  fun deleteByProfile(profileId: Long): Completable
}
