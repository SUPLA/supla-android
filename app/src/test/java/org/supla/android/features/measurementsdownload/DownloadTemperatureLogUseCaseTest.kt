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
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class DownloadTemperatureLogUseCaseTest {

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @Mock
  private lateinit var temperatureLogRepository: TemperatureLogRepository

  @InjectMocks
  private lateinit var useCase: DownloadTemperatureLogUseCase

  @Test
  fun `should load measurements from cloud`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<TemperatureLogEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(temperatureLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))

    whenever(temperatureLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(temperatureLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureLogRepository).findCount(remoteId, profileId)
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(temperatureLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
    verify(temperatureLogRepository).map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))

    val captor = argumentCaptor<List<TemperatureLogEntity>>()
    verify(temperatureLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureLogEntity(null, remoteId, measurementDate, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
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
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
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
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    whenever(temperatureLogRepository.getMeasurements(cloudService, remoteId, 0))
      .thenReturn(Observable.just(emptyList()))
    whenever(temperatureLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(temperatureLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(temperatureLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureLogRepository).findCount(remoteId, profileId)
    verify(temperatureLogRepository).delete(remoteId, profileId)
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureLogRepository).getMeasurements(cloudService, remoteId, 0)

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
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

    whenever(temperatureLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureLogRepository).findCount(remoteId, profileId)
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
  }

  @Test
  fun `should clean DB and load measurements from cloud`() {
    // given
    val remoteId = 222
    val profileId = 333L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<TemperatureLogEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(temperatureLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(temperatureLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(temperatureLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(temperatureLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(temperatureLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(temperatureLogRepository).findMinTimestamp(remoteId, profileId)
    verify(temperatureLogRepository).findOldestEntity(remoteId, profileId)
    verify(temperatureLogRepository).findCount(remoteId, profileId)
    verify(temperatureLogRepository).delete(remoteId, profileId)
    verify(temperatureLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(temperatureLogRepository).map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))
    verify(temperatureLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(temperatureLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<TemperatureLogEntity>>()
    verify(temperatureLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureLogEntity(null, remoteId, measurementDate, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, temperatureLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = TemperatureMeasurement(measurementDate, 23f)
    whenever(temperatureLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()))
      .thenReturn(Observable.just(listOf(measurement)))
    whenever(temperatureLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()))
      .thenReturn(Observable.just(emptyList()))
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

    whenever(temperatureLogRepository.getInitialMeasurements(cloudService, remoteId)).thenReturn(response)
  }

  private fun mockEntityMapping(remoteId: Int, profileId: Long) {
    whenever(temperatureLogRepository.map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))).thenAnswer {
      val entry = it.getArgument<TemperatureMeasurement>(0)
      return@thenAnswer TemperatureLogEntity(
        id = null,
        channelId = remoteId,
        date = entry.date,
        temperature = entry.temperature,
        profileId = profileId
      )
    }
  }
}
