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
import org.supla.android.data.source.local.entity.custom.BalancedValue
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.ALL_COLUMNS
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_COUNTER_RESET
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_FAE_BALANCED
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_GROUPING_STRING
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_MANUALLY_COMPLEMENTED
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE1_FAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE1_FRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE1_RAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE1_RRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE2_FAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE2_FRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE2_RAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE2_RRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE3_FAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE3_FRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE3_RAE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PHASE3_RRE
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_RAE_BALANCED
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.COLUMN_TIMESTAMP
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity.Companion.TABLE_NAME

@Dao
interface ElectricityMeterLogDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(entity: List<ElectricityMeterLogEntity>): Completable

  @Query("SELECT MIN(date) FROM $TABLE_NAME WHERE $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_PROFILE_ID = :profileId")
  fun findMinTimestamp(channelId: Int, profileId: Long): Single<Long>

  @Query("SELECT MAX(date) FROM $TABLE_NAME WHERE $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_PROFILE_ID = :profileId")
  fun findMaxTimestamp(channelId: Int, profileId: Long): Single<Long>

  @Query(
    """
      SELECT $ALL_COLUMNS FROM $TABLE_NAME 
      WHERE $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_PROFILE_ID = :profileId 
      ORDER BY $COLUMN_TIMESTAMP DESC 
      LIMIT 1
    """
  )
  fun findOldestEntity(channelId: Int, profileId: Long): Maybe<ElectricityMeterLogEntity>

  @Query(
    """
    SELECT COUNT(_id) FROM $TABLE_NAME
    WHERE $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_PROFILE_ID = :profileId AND $COLUMN_MANUALLY_COMPLEMENTED = :manuallyComplemented
    """
  )
  fun findCount(channelId: Int, profileId: Long, manuallyComplemented: Boolean = false): Maybe<Int>

  @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_CHANNEL_ID = :channelId AND $COLUMN_PROFILE_ID = :profileId")
  fun delete(channelId: Int, profileId: Long): Completable

  @Query(
    """
      SELECT $ALL_COLUMNS FROM $TABLE_NAME
      WHERE $COLUMN_CHANNEL_ID = :channelId 
        AND $COLUMN_PROFILE_ID = :profileId 
        AND $COLUMN_TIMESTAMP >= :startDate 
        AND $COLUMN_TIMESTAMP <= :endDate
      ORDER BY $COLUMN_TIMESTAMP ASC
    """
  )
  fun findMeasurements(channelId: Int, profileId: Long, startDate: Long, endDate: Long): Observable<List<ElectricityMeterLogEntity>>

  @Query(
    """
      SELECT
        $COLUMN_ID, 
        $COLUMN_CHANNEL_ID, 
        MIN($COLUMN_TIMESTAMP) as $COLUMN_TIMESTAMP, 
        SUM($COLUMN_PHASE1_FAE) as $COLUMN_PHASE1_FAE, 
        SUM($COLUMN_PHASE1_RAE) as $COLUMN_PHASE1_RAE, 
        SUM($COLUMN_PHASE1_FRE) as $COLUMN_PHASE1_FRE, 
        SUM($COLUMN_PHASE1_RRE) as $COLUMN_PHASE1_RRE, 
        SUM($COLUMN_PHASE2_FAE) as $COLUMN_PHASE2_FAE, 
        SUM($COLUMN_PHASE2_RAE) as $COLUMN_PHASE2_RAE, 
        SUM($COLUMN_PHASE2_FRE) as $COLUMN_PHASE2_FRE, 
        SUM($COLUMN_PHASE2_RRE) as $COLUMN_PHASE2_RRE, 
        SUM($COLUMN_PHASE3_FAE) as $COLUMN_PHASE3_FAE, 
        SUM($COLUMN_PHASE3_RAE) as $COLUMN_PHASE3_RAE, 
        SUM($COLUMN_PHASE3_FRE) as $COLUMN_PHASE3_FRE, 
        SUM($COLUMN_PHASE3_RRE) as $COLUMN_PHASE3_RRE, 
        SUM($COLUMN_FAE_BALANCED) as $COLUMN_FAE_BALANCED,
        SUM($COLUMN_RAE_BALANCED) as $COLUMN_RAE_BALANCED, 
        0 as $COLUMN_MANUALLY_COMPLEMENTED, 
        MAX($COLUMN_COUNTER_RESET) as $COLUMN_COUNTER_RESET, 
        $COLUMN_PROFILE_ID,
        SUBSTR($COLUMN_GROUPING_STRING, :groupingStart, :groupingLength) as $COLUMN_GROUPING_STRING 
      FROM $TABLE_NAME
      WHERE $COLUMN_CHANNEL_ID = :channelId 
        AND $COLUMN_PROFILE_ID = :profileId 
        AND $COLUMN_TIMESTAMP >= :startDate 
        AND $COLUMN_TIMESTAMP <= :endDate
      GROUP BY SUBSTR($COLUMN_GROUPING_STRING, :groupingStart, :groupingLength)
      ORDER BY $COLUMN_TIMESTAMP ASC
    """
  )
  fun findMeasurementsGrouped(
    channelId: Int,
    profileId: Long,
    startDate: Long,
    endDate: Long,
    groupingStart: Int,
    groupingLength: Int
  ): Observable<List<ElectricityMeterLogEntity>>

  @Query(
    """
      SELECT
        $COLUMN_TIMESTAMP, 
        SUM(
          CASE 
            WHEN active > 0 THEN active
            ELSE 0
          END
        ) as consumption,
        SUM(
          CASE 
            WHEN active < 0 THEN -active
            ELSE 0
          END
        ) as production,
        $COLUMN_GROUPING_STRING
        FROM
        (
          SELECT
            MIN($COLUMN_TIMESTAMP) as $COLUMN_TIMESTAMP, 
            COALESCE(SUM($COLUMN_PHASE1_FAE), 0) 
              + COALESCE(SUM($COLUMN_PHASE2_FAE), 0) 
              + COALESCE(SUM($COLUMN_PHASE3_FAE), 0) 
              - COALESCE(SUM($COLUMN_PHASE1_RAE), 0) 
              - COALESCE(SUM($COLUMN_PHASE2_RAE), 0) 
              - COALESCE(SUM($COLUMN_PHASE3_RAE), 0) as active,
            $COLUMN_GROUPING_STRING
          FROM $TABLE_NAME
          WHERE $COLUMN_CHANNEL_ID = :channelId 
            AND $COLUMN_PROFILE_ID = :profileId 
            AND $COLUMN_TIMESTAMP >= :startDate 
            AND $COLUMN_TIMESTAMP <= :endDate
          GROUP BY SUBSTR($COLUMN_GROUPING_STRING, 1, 10)
          ORDER BY $COLUMN_TIMESTAMP ASC
        )
        GROUP BY SUBSTR($COLUMN_GROUPING_STRING, :groupingStart, :groupingLength)
        ORDER BY $COLUMN_TIMESTAMP ASC
    """
  )
  fun findMeasurementsHourlyGrouped(
    channelId: Int,
    profileId: Long,
    startDate: Long,
    endDate: Long,
    groupingStart: Int,
    groupingLength: Int
  ): Observable<List<BalancedValue>>

  @Query("SELECT COUNT($COLUMN_ID) FROM $TABLE_NAME")
  fun count(): Observable<Int>
}
