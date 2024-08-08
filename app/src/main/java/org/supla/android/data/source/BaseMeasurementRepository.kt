package org.supla.android.data.source
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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.entity.measurements.BaseLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.Measurement
import retrofit2.Response

abstract class BaseMeasurementRepository<T : Measurement, U : BaseLogEntity> {

  abstract fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<T>>

  abstract fun getMeasurements(cloudService: SuplaCloudService, remoteId: Int, afterTimestamp: Long): Observable<List<T>>

  abstract fun map(entry: T, remoteId: Int, profileId: Long): U

  abstract fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long>

  abstract fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long>

  abstract fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<U>

  abstract fun delete(remoteId: Int, profileId: Long): Completable

  abstract fun findCount(remoteId: Int, profileId: Long): Maybe<Int>

  abstract fun insert(entries: List<U>): Completable
}
