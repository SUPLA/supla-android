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

import androidx.room.rxjava3.EmptyResultSetException
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.RoomTemperatureLogDao
import org.supla.android.data.source.local.entity.TemperatureLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Call
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class TemperatureLogRepositoryTest {

  @Mock
  private lateinit var roomTemperatureLogDao: RoomTemperatureLogDao

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @InjectMocks
  private lateinit var repository: TemperatureLogRepository

  @Test
  fun `should find measurements in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val startDate = date(2023, 1, 1)
    val endDate = date(2023, 2, 1)
    val entity: TemperatureLogEntity = mockk()
    whenever(roomTemperatureLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time))
      .thenReturn(Observable.just(listOf(entity)))

    // when
    val testObserver = repository.findMeasurements(remoteId, profileId, startDate, endDate).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(listOf(entity))

    verify(roomTemperatureLogDao).findMeasurements(remoteId, profileId, startDate.time, endDate.time)
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should delete measurements from DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    whenever(roomTemperatureLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val testObserver = repository.delete(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(roomTemperatureLogDao).delete(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find count in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val count = 123
    whenever(roomTemperatureLogDao.findCount(remoteId, profileId))
      .thenReturn(Maybe.just(count))

    // when
    val testObserver = repository.findCount(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(count)

    verify(roomTemperatureLogDao).findCount(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find min timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(roomTemperatureLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMinTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(roomTemperatureLogDao).findMinTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find max timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(roomTemperatureLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMaxTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(roomTemperatureLogDao).findMaxTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should insert to DB`() {
    // given
    val entity: TemperatureLogEntity = mockk()
    whenever(roomTemperatureLogDao.insert(listOf(entity)))
      .thenReturn(Completable.complete())

    // when
    val testObserver = repository.insert(listOf(entity)).test()

    // then
    testObserver.assertComplete()

    verify(roomTemperatureLogDao).insert(listOf(entity))
    verifyNoMoreInteractions(roomTemperatureLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should load measurements from cloud`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(roomTemperatureLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))

    whenever(roomTemperatureLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(roomTemperatureLogDao.insert(any())).thenReturn(Completable.complete())

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findCount(remoteId, profileId)

    val captor = argumentCaptor<List<TemperatureLogEntity>>()
    verify(roomTemperatureLogDao).insert(captor.capture())
    val result = captor.firstValue
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(
      TemperatureLogEntity(null, remoteId, measurementDate, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureLogDao)
  }

  @Test
  fun `should stop loading when initial request to cloud service failed`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()
    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate, httpCode = 500)

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    verify(suplaCloudServiceProvider).provide()
    verifyNoMoreInteractions(suplaCloudServiceProvider)
    verifyZeroInteractions(roomTemperatureLogDao)
  }

  @Test
  fun `should stop loading when there is no header with total count`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate, totalCount = null)

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    verify(suplaCloudServiceProvider).provide()
    verifyNoMoreInteractions(suplaCloudServiceProvider)
    verifyZeroInteractions(roomTemperatureLogDao)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    every {
      cloudService.getThermometerMeasurements(remoteId, limit = 5000, afterTimestamp = 0)
    } returns Observable.just(emptyList())

    whenever(roomTemperatureLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(roomTemperatureLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(roomTemperatureLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findCount(remoteId, profileId)
    verify(roomTemperatureLogDao).delete(remoteId, profileId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureLogDao)
  }

  @Test
  fun `should skip import when database and cloud count are same`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(roomTemperatureLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findCount(remoteId, profileId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureLogDao)
  }

  @Test
  fun `should clean DB and load measurements from cloud`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(roomTemperatureLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(roomTemperatureLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(roomTemperatureLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(roomTemperatureLogDao.insert(any())).thenReturn(Completable.complete())

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureLogDao).findCount(remoteId, profileId)
    verify(roomTemperatureLogDao).delete(remoteId, profileId)

    val captor = argumentCaptor<List<TemperatureLogEntity>>()
    verify(roomTemperatureLogDao).insert(captor.capture())
    val result = captor.firstValue
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(
      TemperatureLogEntity(null, remoteId, measurementDate, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureLogDao)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = TemperatureMeasurement(measurementDate, 23f)
    every {
      cloudService.getThermometerMeasurements(remoteId, limit = 5000, afterTimestamp = lastDbDate.toTimestamp())
    } returns Observable.just(listOf(measurement))
    every {
      cloudService.getThermometerMeasurements(remoteId, limit = 5000, afterTimestamp = measurementDate.toTimestamp())
    } returns Observable.just(emptyList())
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    date: Date? = null,
    totalCount: Int? = 100,
    httpCode: Int = 200
  ) {
    val response: Response<List<TemperatureMeasurement>> = mockk()
    every { response.code() } returns httpCode
    if (date != null) {
      val initialMeasurement: TemperatureMeasurement = mockk()
      every { initialMeasurement.date } returns date
      every { response.body() } returns listOf(initialMeasurement)
    } else {
      every { response.body() } returns emptyList()
    }
    if (totalCount != null) {
      every { response.headers() } returns Headers.headersOf("X-Total-Count", "$totalCount")
    }

    val call: Call<List<TemperatureMeasurement>> = mockk()
    every { call.execute() } returns response
    every { cloudService.getInitialThermometerMeasurements(remoteId) } returns call
  }
}
