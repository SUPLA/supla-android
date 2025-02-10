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
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.data.source.local.entity.NotificationEntity.Companion.ALL_COLUMNS_STRING
import org.supla.android.data.source.local.entity.NotificationEntity.Companion.COLUMN_DATE
import org.supla.android.data.source.local.entity.NotificationEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.NotificationEntity.Companion.TABLE_NAME
import java.time.LocalDateTime

@Dao
interface NotificationDao {

  @Insert
  fun insert(notification: NotificationEntity): Completable

  @Query("SELECT $ALL_COLUMNS_STRING FROM $TABLE_NAME ORDER BY $COLUMN_DATE DESC")
  fun loadAll(): Observable<List<NotificationEntity>>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
  fun delete(id: Long): Completable

  @Query("DELETE FROM $TABLE_NAME")
  fun deleteAll(): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_DATE < :olderThan")
  fun deleteAll(olderThan: LocalDateTime): Completable

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
