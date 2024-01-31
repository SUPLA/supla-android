package org.supla.android.features.measurementsdownload

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
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeasurement
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class DownloadGeneralPurposeMeasurementLogUseCaseTest {

  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @Mock
  private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @InjectMocks
  private lateinit var useCase: DownloadGeneralPurposeMeasurementLogUseCase

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

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))

    whenever(generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeasurementLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeasurementLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeasurementLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeasurementLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())
    verify(generalPurposeMeasurementLogRepository).map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))

    val captor = argumentCaptor<List<GeneralPurposeMeasurementEntity>>()
    verify(generalPurposeMeasurementLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeasurementEntity(null, remoteId, measurementDate, 23f, 21f, 25f, 22f, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    whenever(generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, 0))
      .thenReturn(Observable.just(emptyList()))
    whenever(generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(generalPurposeMeasurementLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeasurementLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).delete(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeasurementLogRepository).getMeasurements(cloudService, remoteId, 0)

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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

    whenever(generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeasurementLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
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

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, date(2023, 10, 5))

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(generalPurposeMeasurementLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeasurementLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(generalPurposeMeasurementLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeasurementLogRepository.insert(any())).thenReturn(Completable.complete())
    mockEntityMapping(remoteId, profileId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeasurementLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).delete(remoteId, profileId)
    verify(generalPurposeMeasurementLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeasurementLogRepository).map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))
    verify(generalPurposeMeasurementLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeasurementLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeasurementEntity>>()
    verify(generalPurposeMeasurementLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeasurementEntity(null, remoteId, measurementDate, 23f, 21f, 25f, 22f, 23f, profileId)
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeasurementLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = GeneralPurposeMeasurement(measurementDate, 23f, 21f, 25f, 22f, 23f)
    whenever(generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()))
      .thenReturn(Observable.just(listOf(measurement)))
    whenever(generalPurposeMeasurementLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()))
      .thenReturn(Observable.just(emptyList()))
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

    whenever(generalPurposeMeasurementLogRepository.getInitialMeasurements(cloudService, remoteId)).thenReturn(response)
  }

  private fun mockEntityMapping(remoteId: Int, profileId: Long) {
    whenever(generalPurposeMeasurementLogRepository.map(any(), ArgumentMatchers.eq(remoteId), ArgumentMatchers.eq(profileId))).thenAnswer {
      val entry = it.getArgument<GeneralPurposeMeasurement>(0)
      return@thenAnswer GeneralPurposeMeasurementEntity(
        id = null,
        channelId = remoteId,
        date = entry.date,
        valueAverage = entry.average,
        valueMin = entry.min,
        valueMax = entry.max,
        valueOpen = entry.open,
        valueClose = entry.close,
        profileId = profileId
      )
    }
  }
}
