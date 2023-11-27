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
import org.supla.android.data.source.local.RoomTemperatureAndHumidityLogDao
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Call
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class TemperatureAndHumidityLogRepositoryTest {

  @Mock
  private lateinit var roomTemperatureAndHumidityLogDao: RoomTemperatureAndHumidityLogDao

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @InjectMocks
  private lateinit var repository: TemperatureAndHumidityLogRepository

  @Test
  fun `should find measurements in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val startDate = date(2023, 1, 1)
    val endDate = date(2023, 2, 1)
    val entity: TemperatureAndHumidityLogEntity = mockk()
    whenever(roomTemperatureAndHumidityLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time))
      .thenReturn(Observable.just(listOf(entity)))

    // when
    val testObserver = repository.findMeasurements(remoteId, profileId, startDate, endDate).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(listOf(entity))

    verify(roomTemperatureAndHumidityLogDao).findMeasurements(remoteId, profileId, startDate.time, endDate.time)
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should delete measurements from DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    whenever(roomTemperatureAndHumidityLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())

    // when
    val testObserver = repository.delete(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(roomTemperatureAndHumidityLogDao).delete(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find count in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val count = 123
    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId))
      .thenReturn(Maybe.just(count))

    // when
    val testObserver = repository.findCount(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(count)

    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find min timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMinTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(roomTemperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should find max timestamp in DB`() {
    // given
    val remoteId = 123
    val profileId = 234L
    val timestamp = 123L
    whenever(roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(timestamp))

    // when
    val testObserver = repository.findMaxTimestamp(remoteId, profileId).test()

    // then
    testObserver.assertComplete()
    testObserver.assertResult(timestamp)

    verify(roomTemperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
    verifyZeroInteractions(suplaCloudServiceProvider)
  }

  @Test
  fun `should insert to DB`() {
    // given
    val entity: TemperatureAndHumidityLogEntity = mockk()
    whenever(roomTemperatureAndHumidityLogDao.insert(listOf(entity)))
      .thenReturn(Completable.complete())

    // when
    val testObserver = repository.insert(listOf(entity)).test()

    // then
    testObserver.assertComplete()

    verify(roomTemperatureAndHumidityLogDao).insert(listOf(entity))
    verifyNoMoreInteractions(roomTemperatureAndHumidityLogDao)
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

    whenever(roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))

    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(roomTemperatureAndHumidityLogDao.insert(any())).thenReturn(Completable.complete())

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(roomTemperatureAndHumidityLogDao).insert(captor.capture())
    val result = captor.firstValue
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureAndHumidityLogDao)
  }

  @Test
  fun `should load measurements from cloud when local database is empty`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, Date(0), remoteId, cloudService)

    whenever(roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))

    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(0))
    whenever(roomTemperatureAndHumidityLogDao.insert(any())).thenReturn(Completable.complete())

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(roomTemperatureAndHumidityLogDao).insert(captor.capture())
    val result = captor.firstValue
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureAndHumidityLogDao)
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
    verifyZeroInteractions(roomTemperatureAndHumidityLogDao)
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
    verifyZeroInteractions(roomTemperatureAndHumidityLogDao)
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
      cloudService.getThermometerWithHumidityMeasurements(remoteId, limit = 5000, afterTimestamp = 0)
    } returns Observable.just(emptyList())

    whenever(roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(roomTemperatureAndHumidityLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).delete(remoteId, profileId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureAndHumidityLogDao)
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

    whenever(roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureAndHumidityLogDao)
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

    whenever(roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(roomTemperatureAndHumidityLogDao.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(roomTemperatureAndHumidityLogDao.insert(any())).thenReturn(Completable.complete())

    // when
    val testObserver = repository.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(roomTemperatureAndHumidityLogDao).findMinTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findMaxTimestamp(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).findCount(remoteId, profileId)
    verify(roomTemperatureAndHumidityLogDao).delete(remoteId, profileId)

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(roomTemperatureAndHumidityLogDao).insert(captor.capture())
    val result = captor.firstValue
    assertThat(result).hasSize(1)
    assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, roomTemperatureAndHumidityLogDao)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = TemperatureAndHumidityMeasurement(measurementDate, 23f, 81f)
    every {
      cloudService.getThermometerWithHumidityMeasurements(remoteId, limit = 5000, afterTimestamp = lastDbDate.toTimestamp())
    } returns Observable.just(listOf(measurement))
    every {
      cloudService.getThermometerWithHumidityMeasurements(remoteId, limit = 5000, afterTimestamp = measurementDate.toTimestamp())
    } returns Observable.just(emptyList())
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    date: Date? = null,
    totalCount: Int? = 100,
    httpCode: Int = 200
  ) {
    val response: Response<List<TemperatureAndHumidityMeasurement>> = mockk()
    every { response.code() } returns httpCode
    if (date != null) {
      val initialMeasurement: TemperatureAndHumidityMeasurement = mockk()
      every { initialMeasurement.date } returns date
      every { response.body() } returns listOf(initialMeasurement)
    } else {
      every { response.body() } returns emptyList()
    }
    if (totalCount != null) {
      every { response.headers() } returns Headers.headersOf("X-Total-Count", "$totalCount")
    }

    val call: Call<List<TemperatureAndHumidityMeasurement>> = mockk()
    every { call.execute() } returns response
    every { cloudService.getInitialThermometerWithHumidityMeasurements(remoteId) } returns call
  }
}
