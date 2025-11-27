package org.supla.android.features.measurementsdownload

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

class DownloadGeneralPurposeMeterLogUseCaseTest {
  @MockK
  private lateinit var suplaCloudServiceProvider: SuplaCloudService.Provider

  @MockK
  private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @MockK
  private lateinit var channelConfigRepository: ChannelConfigRepository

  @InjectMockKs
  private lateinit var useCase: DownloadGeneralPurposeMeterLogUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
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
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should clean database when there is no entry in initial request`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, totalCount = 0)

    every { generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, 0) } returns Observable.just(emptyList())
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { generalPurposeMeterLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.just(mockk<SuplaChannelGeneralPurposeMeterConfig>())

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.delete(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, 0)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(100)
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load data when header with total count contains only small letters`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val lastDbDate = date(2023, 10, 1)
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate, totalCountHeader = "x-total-count")

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns
      Single.just(date(2023, 10, 1).time)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(100)
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should load measurements from cloud when db is empty`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, Date(0), remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { generalPurposeMeterLogRepository.delete(remoteId, profileId) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.delete(remoteId, profileId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, 0)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
    }

    val result = captor.captured
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
        groupingString = "2023110104003",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 3)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns Single.just(lastDbDate.time)
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
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
        groupingString = "2023110103003",
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
        groupingString = "2023110103003",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 0, 30)
    val secondMeasurementDate = date(2023, 10, 1, 0, 40)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns Single.just(lastDbDate.time)
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockChannelConfig(profileId, remoteId, fillData = true)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
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
        groupingString = "2023110100103",
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
        groupingString = "2023110100203",
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
        groupingString = "2023110100303",
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
        groupingString = "2023110100403",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns Single.just(lastDbDate.time)
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockChannelConfig(profileId, remoteId)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
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
        groupingString = "2023110103003",
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
        groupingString = "2023110104003",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, lastDbDate)

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, lastDbDate, remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns Single.just(lastDbDate.time)
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(lastEntity)
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockChannelConfig(profileId, remoteId, counterType = SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp())
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
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
        groupingString = "2023110103003",
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
        groupingString = "2023110104003",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
  }

  @Test
  fun `should not store positive values when counter type is decrement`() {
    // given
    val remoteId = 123
    val profileId = 321L
    val cloudService: SuplaCloudService = mockk()

    every { suplaCloudServiceProvider.provide() } returns cloudService
    mockInitialCall(remoteId, cloudService, Date(0))

    val firstMeasurementDate = date(2023, 10, 1, 3)
    val secondMeasurementDate = date(2023, 10, 1, 4)
    mockMeasurementsCall(firstMeasurementDate, secondMeasurementDate, Date(0), remoteId, cloudService)

    every { generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId) } returns Single.error(EmptyResultSetException(""))
    every { generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    every { generalPurposeMeterLogRepository.findCount(remoteId, profileId) } returns Maybe.just(50)
    every { generalPurposeMeterLogRepository.insert(any()) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId) } returns Single.just(0)
    mockChannelConfig(profileId, remoteId, counterType = SuplaChannelConfigMeterCounterType.ALWAYS_DECREMENT)

    // when
    val testObserver = useCase.loadMeasurements(remoteId, profileId).test()

    // then
    testObserver.assertComplete()

    val captor = slot<List<GeneralPurposeMeterEntity>>()
    io.mockk.verify {
      suplaCloudServiceProvider.provide()
      generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)
      generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId)
      generalPurposeMeterLogRepository.findCount(remoteId, profileId)
      generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, 0)
      generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp())
      generalPurposeMeterLogRepository.insert(capture(captor))
      generalPurposeMeterLogRepository.findCountWithoutGroupingString(remoteId, profileId)
    }

    val result = captor.captured
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
        groupingString = "2023110104003",
        profileId = profileId
      )
    )

    confirmVerified(suplaCloudServiceProvider, generalPurposeMeterLogRepository)
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

    every { generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, lastDbDate.toTimestamp()) } returns
      Observable.just(listOf(measurement1, measurement2))
    every { generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, secondMeasurementDate.toTimestamp()) } returns
      Observable.just(emptyList())
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

    every { generalPurposeMeterLogRepository.getInitialMeasurements(cloudService, remoteId) } returns response
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
    every { channelConfigRepository.findChannelConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER) } returns
      Single.just(config)
  }
}
