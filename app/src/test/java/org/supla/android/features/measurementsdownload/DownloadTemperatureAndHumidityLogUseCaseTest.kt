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
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.Headers
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

class DownloadTemperatureAndHumidityLogUseCaseTest {

  @MockK
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @MockK
  private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @InjectMockKs
  private lateinit var useCase: DownloadTemperatureAndHumidityLogUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    every { temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)

    every { temperatureAndHumidityLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { temperatureAndHumidityLogRepository.insert(any()) } returns Completable.complete()
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<TemperatureAndHumidityLogEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)
      temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId)
      temperatureAndHumidityLogRepository.findCount(remoteId, profileId)
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
      temperatureAndHumidityLogRepository.map(any(), eq("2023110103003"), eq(remoteId), eq(profileId))
      temperatureAndHumidityLogRepository.insert(capture(captor))
    }

    val result = captor.captured
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, "2023110103003", profileId)
    )

    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should load measurements from cloud when local database is empty`() {
    // given
    val remoteId = 222
    val profileId = 333L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, Date(0), remoteId, cloudService)

    every { temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.error(EmptyResultSetException(""))
    every { temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()

    every { temperatureAndHumidityLogRepository.findCount(remoteId, profileId) } returns Maybe.just(0)
    every { temperatureAndHumidityLogRepository.insert(any()) } returns Completable.complete()
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<TemperatureAndHumidityLogEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)
      temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId)
      temperatureAndHumidityLogRepository.findCount(remoteId, profileId)
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, 0)
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
      temperatureAndHumidityLogRepository.map(any(), eq("2023110103003"), eq(remoteId), eq(profileId))
      temperatureAndHumidityLogRepository.insert(capture(captor))
    }

    val result = captor.captured
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, "2023110103003", profileId)
    )

    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should stop loading when initial request to cloud service failed`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate, httpCode = 500)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
    }
    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should stop loading when there is no header with total count`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate, totalCount = null)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertError(IllegalStateException::class.java)

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
    }
    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    every { temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, 0) } returns
      Observable.just(emptyList())
    every { temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { temperatureAndHumidityLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { temperatureAndHumidityLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId)
      temperatureAndHumidityLogRepository.findCount(remoteId, profileId)
      temperatureAndHumidityLogRepository.delete(remoteId, profileId)
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, 0)
    }

    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  @Test
  fun `should skip import when database and cloud count are same`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    every { temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { temperatureAndHumidityLogRepository.findCount(remoteId, profileId) } returns Maybe.just(100)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)
      temperatureAndHumidityLogRepository.findCount(remoteId, profileId)
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
    }

    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    every { temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { temperatureAndHumidityLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { temperatureAndHumidityLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { temperatureAndHumidityLogRepository.insert(any()) } returns Completable.complete()
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<TemperatureAndHumidityLogEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)
      temperatureAndHumidityLogRepository.findOldestEntity(remoteId, profileId)
      temperatureAndHumidityLogRepository.findCount(remoteId, profileId)
      temperatureAndHumidityLogRepository.delete(remoteId, profileId)
      temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId)
      temperatureAndHumidityLogRepository.map(any(), eq("2023110103003"), eq(remoteId), eq(profileId))
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
      temperatureAndHumidityLogRepository.insert(capture(captor))
    }

    val result = captor.captured
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      TemperatureAndHumidityLogEntity(null, remoteId, measurementDate, 23f, 81f, "2023110103003", profileId)
    )

    confirmVerified(suplaCloudServiceProvider, temperatureAndHumidityLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = TemperatureAndHumidityMeasurement(measurementDate, 23f, 81f)
    every { temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()) } returns
      Observable.just(listOf(measurement))
    every { temperatureAndHumidityLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()) } returns
      Observable.just(emptyList())
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

    every { temperatureAndHumidityLogRepository.getInitialMeasurements(cloudService, remoteId) } returns response
  }

  private fun mockEntityMapping(remoteId: Int, profileId: Long) {
    every { temperatureAndHumidityLogRepository.map(any(), any(), eq(remoteId), eq(profileId)) } answers {
      val entry = it.invocation.args[0] as TemperatureAndHumidityMeasurement
      val groupingString = it.invocation.args[1] as String

      TemperatureAndHumidityLogEntity(
        id = null,
        channelId = remoteId,
        date = entry.date,
        temperature = entry.temperature,
        humidity = entry.humidity,
        groupingString = groupingString,
        profileId = profileId
      )
    }
  }
}
