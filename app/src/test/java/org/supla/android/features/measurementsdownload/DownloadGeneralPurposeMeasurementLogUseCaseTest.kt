package org.supla.android.features.measurementsdownload

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
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

class DownloadGeneralPurposeMeasurementLogUseCaseTest {

  @MockK
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @MockK
  private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @InjectMockKs
  private lateinit var useCase: DownloadGeneralPurposeMeasurementLogUseCase

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
    val lastEntity = mockk<GeneralPurposeMeasurementEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId) } returns
      Maybe.just(lastEntity)

    every { generalPurposeMeasurementLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeasurementLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeasurementEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
      generalPurposeMeasurementLogRepository.map(any(), eq("2023110103003"), eq(remoteId), eq(profileId))
      generalPurposeMeasurementLogRepository.insert(capture(captor))
      generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeasurementEntity(null, remoteId, measurementDate, 23f, 21f, 25f, 22f, 23f, "2023110103003", profileId)
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
    }
    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
    }
    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    every { generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, 0) } returns
      Observable.just(emptyList())
    every { generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { generalPurposeMeasurementLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { generalPurposeMeasurementLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)
      generalPurposeMeasurementLogRepository.delete(remoteId, profileId)
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, 0)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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

    every { generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { generalPurposeMeasurementLogRepository.findCount(remoteId, profileId) } returns Maybe.just(100)
    every { generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
  }

  @Test
  fun `should clean DB and load measurements from cloud`() {
    // given
    val remoteId = 222
    val profileId = 333L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<GeneralPurposeMeasurementEntity>().apply {
      every { date } returns lastDbDate
    }
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { generalPurposeMeasurementLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { generalPurposeMeasurementLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeasurementLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeasurementEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)
      generalPurposeMeasurementLogRepository.delete(remoteId, profileId)
      generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeasurementLogRepository.map(any(), eq("2023110103003"), eq(remoteId), eq(profileId))
      generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
      generalPurposeMeasurementLogRepository.insert(capture(captor))
      generalPurposeMeasurementLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeasurementEntity(null, remoteId, measurementDate, 23f, 21f, 25f, 22f, 23f, "2023110103003", profileId)
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = GeneralPurposeMeasurement(measurementDate, 23f, 21f, 25f, 22f, 23f)
    every { generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()) } returns
      Observable.just(listOf(measurement))
    every { generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()) } returns
      Observable.just(emptyList())
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    date: Date? = null,
    totalCount: Int? = 100,
    httpCode: Int = 200
  ) {
    val response: Response<List<GeneralPurposeMeasurement>> = mockk()
    every { response.code() } returns httpCode
    if (date != null) {
      val initialMeasurement: GeneralPurposeMeasurement = mockk()
      every { initialMeasurement.date } returns date
      every { response.body() } returns listOf(initialMeasurement)
    } else {
      every { response.body() } returns emptyList()
    }
    if (totalCount != null) {
      every { response.headers() } returns Headers.headersOf("X-Total-Count", "$totalCount")
    }

    every { generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId) } returns response
  }

  private fun mockEntityMapping(remoteId: Int, profileId: Long) {
    every { generalPurposeMeasurementLogRepository.map(any(), any(), eq(remoteId), eq(profileId)) } answers {
      val entry = it.invocation.args[0] as GeneralPurposeMeasurement
      val groupingString = it.invocation.args[1] as String

      GeneralPurposeMeasurementEntity(
        id = null,
        channelId = remoteId,
        date = entry.date,
        valueAverage = entry.average,
        valueMin = entry.min,
        valueMax = entry.max,
        valueOpen = entry.open,
        valueClose = entry.close,
        groupingString = groupingString,
        profileId = profileId
      )
    }
  }
}
