package org.supla.android.features.measurementsdownload

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
    whenever(channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER))
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
  fun `should load data when header with total count contains only small letters`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate, totalCountHeader = "x-total-count")

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

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, Date(0), remoteId, cloudService)

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
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 2f,
        value = 26f,
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
      every { value } returns 10f
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

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
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(2)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = firstMeasurementDate,
        valueIncrement = 14f,
        value = 24f,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 2f,
        value = 26f,
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
      every { value } returns 12f
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 0, 30)
    val secondMeasurementDate = date(2023, 10, 1, 0, 40)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

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
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(4)
    Assertions.assertThat(result).containsExactlyInAnyOrder(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 10),
        valueIncrement = 4f,
        value = 16f,
        manuallyComplemented = true,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 20),
        valueIncrement = 4f,
        value = 20f,
        manuallyComplemented = true,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = date(2023, 10, 1, 0, 30),
        valueIncrement = 4f,
        value = 24f,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 2f,
        value = 26f,
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
      every { value } returns 250f
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

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
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(2)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = firstMeasurementDate,
        valueIncrement = -226f,
        value = 24f,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 2f,
        value = 26f,
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
      every { value } returns 250f
    }
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.just(lastDbDate.time))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.just(lastEntity))
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId, counterType = SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT)

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
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(2)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = firstMeasurementDate,
        valueIncrement = 0f,
        value = 24f,
        manuallyComplemented = false,
        counterReset = true,
        profileId = profileId
      ),
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 2f,
        value = 26f,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should not store positive values when counter type is decrement`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    whenever(suplaCloudServiceProvider.provide()).thenReturn(cloudService)
    mockInitialCall(remoteId, cloudService, Date(0))

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, Date(0), remoteId, cloudService)

    whenever(generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId))
      .thenReturn(Single.error(EmptyResultSetException("")))
    whenever(generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId))
      .thenReturn(Maybe.empty())
    whenever(generalPurposeMeterLogRepository.findCount(remoteId, profileId)).thenReturn(Maybe.just(50))
    whenever(generalPurposeMeterLogRepository.insert(any())).thenReturn(Completable.complete())
    mockChannelConfig(profileId, remoteId, counterType = SuplaChannelConfigMeterCounterType.ALWAYS_DECREMENT)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    verify(suplaCloudServiceProvider).provide()
    verify(generalPurposeMeterLogRepository).findMinTimestamp(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findOldestEntity(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).findCount(remoteId, profileId)
    verify(generalPurposeMeterLogRepository).getInitialMeasurements(cloudService, remoteId)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, 0)
    verify(generalPurposeMeterLogRepository).getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())

    val captor = argumentCaptor<List<GeneralPurposeMeterEntity>>()
    verify(generalPurposeMeterLogRepository).insert(captor.capture())
    val result = captor.firstValue
    Assertions.assertThat(result).hasSize(1)
    Assertions.assertThat(result).containsExactly(
      GeneralPurposeMeterEntity(
        id = null,
        channelId = remoteId,
        date = secondMeasurementDate,
        valueIncrement = 0f,
        value = 26f,
        manuallyComplemented = false,
        counterReset = false,
        profileId = profileId
      )
    )

    verifyNoMoreInteractions(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  private fun mockMeasurementsCall(
    firstMeasurementDate: Date,
    secondMeasurementDate: Date,
    lastDbDate: Date,
    remoteId: Int,
    cloudService: SuplaCloudService
  ) {
    val measurement1 = GeneralPurposeMeter(firstMeasurementDate, 24f)
    val measurement2 = GeneralPurposeMeter(secondMeasurementDate, 26f)
    whenever(generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()))
      .thenReturn(Observable.just(listOf(measurement1, measurement2)))
    whenever(generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp()))
      .thenReturn(Observable.just(emptyList()))
  }

  private fun mockInitialCall(
    remoteId: Int,
    cloudService: SuplaCloudService,
    date: Date? = null,
    totalCount: Int? = 100,
    httpCode: Int = 200,
    totalCountHeader: String = "X-Total-Count"
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
      every { response.headers() } returns Headers.headersOf(totalCountHeader, "$totalCount")
    }

    whenever(generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)).thenReturn(response)
  }

  private fun mockChannelConfig(
    profileId: Long,
    remoteId: Int,
    counterType: SuplaChannelConfigMeterCounterType = SuplaChannelConfigMeterCounterType.INCREMENT_AND_DECREMENT,
    fillData: Boolean = false
  ) {
    val config = mockk<SuplaChannelGeneralPurposeMeterConfig> {
      every { this@mockk.counterType } returns counterType
      every { fillMissingData } returns fillData
    }
    whenever(channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER))
      .thenReturn(Single.just(config))
  }
}
