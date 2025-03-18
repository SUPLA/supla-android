package org.supla.android.data.source.local.dao.measurements
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
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.GroupingStringMigratorDao
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_GROUPING_STRING
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.COLUMN_TIMESTAMP
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity.Companion.TABLE_NAME

@Dao
interface TemperatureAndHumidityLogDao : GroupingStringMigratorDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(entity: List<TemperatureAndHumidityLogEntity>): Completable

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_CHANNEL_ID = :remoteId AND $COLUMN_PROFILE_ID = :profileId")
  suspend fun deleteKtx(remoteId: Int, profileId: Long)

  @Query("SELECT MIN(date) FROM $TABLE_NAME WHERE channelid = :channelId AND profileid = :profileId")
  fun findMinTimestamp(channelId: Int, profileId: Long): Single<Long>

  @Query("SELECT MAX(date) FROM $TABLE_NAME WHERE channelid = :channelId AND profileid = :profileId")
  fun findMaxTimestamp(channelId: Int, profileId: Long): Single<Long>

  @Query("SELECT $ALL_COLUMNS FROM $TABLE_NAME WHERE channelid = :channelId AND profileid = :profileId ORDER BY date DESC LIMIT 1")
  fun findOldestEntity(channelId: Int, profileId: Long): Maybe<TemperatureAndHumidityLogEntity>

  @Query("SELECT COUNT(temperature) FROM $TABLE_NAME WHERE channelid = :channelId AND profileid = :profileId")
  fun findCount(channelId: Int, profileId: Long): Maybe<Int>

  @Query("DELETE FROM $TABLE_NAME WHERE channelid = :channelId AND profileid = :profileId")
  fun delete(channelId: Int, profileId: Long): Completable

  @Query(
    "SELECT $ALL_COLUMNS FROM $TABLE_NAME " +
      "WHERE channelid = :channelId AND profileid = :profileId AND date >= :startDate AND date <= :endDate " +
      "ORDER BY date asc"
  )
  fun findMeasurements(channelId: Int, profileId: Long, startDate: Long, endDate: Long): Observable<List<TemperatureAndHumidityLogEntity>>

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>

  @Query(
    """
      SELECT COUNT($COLUMN_GROUPING_STRING) 
      FROM $TABLE_NAME 
      WHERE $COLUMN_CHANNEL_ID = :remoteId 
        AND $COLUMN_PROFILE_ID = :profileId 
        AND $COLUMN_GROUPING_STRING = ''
    """
  )
  override fun emptyGroupingStringCount(remoteId: Int, profileId: Long): Single<Int>

  @Query(
    """
      UPDATE $TABLE_NAME 
      SET $COLUMN_GROUPING_STRING = 
        STRFTIME('%Y%m%d%H%M', DATETIME($COLUMN_TIMESTAMP/1000, 'unixepoch'), 'localtime') || 
        CASE (STRFTIME('%w', DATETIME($COLUMN_TIMESTAMP/1000, 'unixepoch'), 'localtime')) 
          WHEN '1' THEN '1' 
          WHEN '2' THEN '2' 
          WHEN '3' THEN '3' 
          WHEN '4' THEN '4' 
          WHEN '5' THEN '5' 
          WHEN '6' THEN '6' 
          ELSE '7' 
        END
      WHERE $COLUMN_CHANNEL_ID = :remoteId 
        AND $COLUMN_PROFILE_ID = :profileId 
        AND $COLUMN_GROUPING_STRING = ''
    """
  )
  override fun migrateGroupingString(remoteId: Int, profileId: Long): Completable
}
