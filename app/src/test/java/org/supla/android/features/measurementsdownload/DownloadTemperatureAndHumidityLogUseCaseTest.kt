package org.supla.android.features.measurementsdownload
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
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class DownloadTemperatureAndHumidityLogUseCaseTest {

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @Mock
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @InjectMocks
  private lateinit var useCase: DownloadTemperatureAndHumidityLogUseCase

  @Test
  fun `should load measurements from cloud`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<TemperatureAndHumidityLogEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))

    whenever(temperatureAndHumidityLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(temperatureAndHumidityLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findCount(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
    verify(temperatureAndHumidityLogRepository).map(any(), eq(remoteId), eq(profileId))

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(temperatureAndHumidityLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should load measurements from cloud when local database is empty`() {
    // given
    val remoteId = 222
    val profileId = 333L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, Date(0), remoteId, cloudService)

    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())

    whenever(temperatureAndHumidityLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(0))
    whenever(temperatureAndHumidityLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findCount(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, 0)
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
    verify(temperatureAndHumidityLogRepository).map(any(), eq(remoteId), eq(profileId))

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(temperatureAndHumidityLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
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
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
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
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    whenever(temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, 0))
      .thenReturn(Observable.just(emptyList()))
    whenever(temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(temperatureAndHumidityLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(temperatureAndHumidityLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findCount(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).delete(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, 0)

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
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

    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureAndHumidityLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findCount(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should clean DB and load measurements from cloud`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<TemperatureAndHumidityLogEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(temperatureAndHumidityLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(temperatureAndHumidityLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(temperatureAndHumidityLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureAndHumidityLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).findCount(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).delete(remoteId, profileId)
    verify(temperatureAndHumidityLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureAndHumidityLogRepository).map(any(), eq(remoteId), eq(profileId))
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(temperatureAndHumidityLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<TemperatureAndHumidityLogEntity>>()
    verify(temperatureAndHumidityLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = TemperatureAndHumidityMeasurement(measurementDate, 23f, 81f)
    whenever(temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()))
      .thenReturn(Observable.just(listOf(measurement)))
    whenever(temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()))
      .thenReturn(Observable.just(emptyList()))
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

    whenever(temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)).thenReturn(response)
  }

  private fun mockEntityMapping(remoteId: Int, profileId: Long) {
    whenever(temperatureAndHumidityLogRepository.map(any(), eq(remoteId), eq(profileId))).thenAnswer {
      val entry = it.getArgument<TemperatureAndHumidityMeasurement>(0)
      return@thenAnswer TemperatureAndHumidityLogEntity(
        id = null,
        channelId = remoteId,
        date = entry.date,
        temperature = entry.temperature,
        humidity = entry.humidity,
        profileId = profileId
      )
    }
  }
}
