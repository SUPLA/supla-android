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
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterCounterType
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeter
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import retrofit2.Response
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class DownloadGeneralPurposeMeterLogUseCaseTest {
  @Mock
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @Mock
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @Mock
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @InjectMocks
  private lateinit var useCase: DownloadGeneralPurposeMeterLogUseCase

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
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    whenever(generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, 0))
      .thenReturn(Observable.just(emptyList()))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(generalPurposeMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(channelConfigRepository.findGpmConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER))
      .thenReturn(Single.just(mockk<SuplaChannelGeneralPurposeMeterConfig>()))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).delete(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, 0)

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(date(2023, 10, 1).time))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(100))

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud when db is empty`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, Date(0), remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(generalPurposeMeterLogRepository.delete(remoteId, profileId)).thenReturn(Completable.complete())
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).delete(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, 0)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = measurementDate,
        valueIncrement = 24f,
        counterIncrement = 0,
        value = 24f,
        counter = 0,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud when db has entries`() {
    // given
    val remoteId = 222
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<GeneralPurposeMeterEntity>().apply {
      every { date } returns lastDbDate
      every { valueIncrement } returns 5f
      every { counterIncrement } returns 5
      every { value } returns 10f
      every { counter } returns 10
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = measurementDate,
        valueIncrement = 14f,
        counterIncrement = 0,
        value = 24f,
        counter = 0,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud and fill missing entries`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<GeneralPurposeMeterEntity>().apply {
      every { date } returns lastDbDate
      every { valueIncrement } returns 5f
      every { counterIncrement } returns 5
      every { value } returns 12f
      every { counter } returns 12
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 0, 30)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId, fillData = true)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(3)
    Assertions.assertThat(result).containsExactlyInAnyOrder(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 10),
        valueIncrement = 4f,
        counterIncrement = 0,
        value = 16f,
        counter = 0,
        manuallyComplemented = true,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 20),
        valueIncrement = 4f,
        counterIncrement = 0,
        value = 20f,
        counter = 0,
        manuallyComplemented = true,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 30),
        valueIncrement = 4f,
        counterIncrement = 0,
        value = 24f,
        counter = 0,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud and insert negative increments when reset is not allowed`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<GeneralPurposeMeterEntity>().apply {
      every { date } returns lastDbDate
      every { valueIncrement } returns 5f
      every { counterIncrement } returns 5
      every { value } returns 250f
      every { counter } returns 250
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = measurementDate,
        valueIncrement = -226f,
        counterIncrement = 0,
        value = 24f,
        counter = 0,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud and mark reset`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val lastEntity = mockk<GeneralPurposeMeterEntity>().apply {
      every { date } returns lastDbDate
      every { valueIncrement } returns 5f
      every { counterIncrement } returns 5
      every { value } returns 250f
      every { counter } returns 250
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val measurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(measurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId, allowReset = true)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, measurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = measurementDate,
        valueIncrement = 24f,
        counterIncrement = 0,
        value = 24f,
        counter = 0,
        manuallyComplemented = false,
        counterReset = true,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  private fun mockMeasurementsCall(measurementDate: Date, lastDbDate: Date, remoteId: Int, cloudService: SuplaCloudService) {
    val measurement = GeneralPurposeMeter(measurementDate, 24f)
    whenever(generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()))
      .thenReturn(Observable.just(listOf(measurement)))
    whenever(generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, measurementDate.toTimestamp()))
      .thenReturn(Observable.just(emptyList()))
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    date: Date? = null,
    totalCount: Int? = 100,
    httpCode: Int = 200
  ) {
    val response: Response<List<GeneralPurposeMeter>> = mockk()
    every { response.code() } returns httpCode
    if (date != null) {
      val initialMeasurement: GeneralPurposeMeter = mockk()
      every { initialMeasurement.date } returns date
      every { response.body() } returns listOf(initialMeasurement)
    } else {
      every { response.body() } returns emptyList()
    }
    if (totalCount != null) {
      every { response.headers() } returns Headers.headersOf("X-Total-Count", "$totalCount")
    }

    whenever(generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)).thenReturn(response)
  }

  private fun mockChannelConfig(profileId: Long, remoteId: Int, allowReset: Boolean = false, fillData: Boolean = false) {
    val config = mockk<SuplaChannelGeneralPurposeMeterConfig>() {
      if (allowReset) {
        every { counterType } returns SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT
      } else {
        every { counterType } returns SuplaChannelConfigMeterCounterType.INCREMENT_AND_DECREMENT
      }
      every { fillMissingData } returns fillData
    }
    whenever(channelConfigRepository.findGpmConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER))
      .thenReturn(Single.just(config))
  }
}
