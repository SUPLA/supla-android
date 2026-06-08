package org.supla.android.usecases.list
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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.extensions.date
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements
import org.supla.android.usecases.channel.measurements.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import java.time.ZonedDateTime
import java.util.Date

class RefreshElectricityMeterAggregatedValueUseCaseTest {

  @MockK
  private lateinit var loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase

  @MockK
  private lateinit var electricityMeterLogRepository: ElectricityMeterLogRepository

  @MockK
  private lateinit var channelValueRepository: ChannelValueRepository

  @MockK
  private lateinit var userStateHolder: UserStateHolder

  @MockK
  private lateinit var dateProvider: DateProvider

  @InjectMockKs
  private lateinit var useCase: RefreshElectricityMeterAggregatedValueUseCase

  private val profileId = 1L
  private val remoteId = 100

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should return early when settings do not use aggregated value`() = runTest {
    // given
    every {
      userStateHolder.getElectricityMeterSettings(profileId, remoteId)
    } returns settings(
      metricOnList = ElectricityMeterMeasurementType.POWER_ACTIVE,
      metricOnListAggregation = ListValueAggregation.NO_AGGREGATION
    )

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify { userStateHolder.getElectricityMeterSettings(profileId, remoteId) }
    verify(exactly = 0) { electricityMeterLogRepository.findOldestEntity(any(), any()) }
    coVerify(exactly = 0) { channelValueRepository.updateAggregatedValue(any(), any(), any()) }
    confirmVerified(userStateHolder, electricityMeterLogRepository, channelValueRepository)
  }

  @Test
  fun `should update with NO_VALUE_TEXT when no logs are found in database`() = runTest {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) }
  }

  @Test
  fun `should update with NO_VALUE_TEXT when aggregation start date is unavailable`() = runTest {
    // given
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.NO_AGGREGATION
    )
    every { dateProvider.currentDateTime } returns ZonedDateTime.now()
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify {
      dateProvider.currentDateTime
      userStateHolder.getElectricityMeterSettings(profileId, remoteId)
      electricityMeterLogRepository.findOldestEntity(remoteId, profileId)
    }
    coVerify { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) }
    confirmVerified(electricityMeterLogRepository, channelValueRepository, userStateHolder, dateProvider)
  }

  @Test
  fun `should update with formatted forward active energy aggregated value`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR,
      metricOnListBalancing = ElectricityMeterBalanceType.HOURLY
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every {
      loadElectricityMeterMeasurementsUseCase.invoke(profileId, remoteId, ElectricityMeterBalanceType.HOURLY, startTimestamp)
    } returns Maybe.just(ElectricityMeasurements(forwardActiveEnergy = 12.34f, reversedActiveEnergy = 1f))
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(
        profileId,
        remoteId,
        match { it == "12.34 kWh" }
      )
    }
  }

  @Test
  fun `should update with formatted reverse active energy aggregated value`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.REVERSE_ACTIVE_ENERGY,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR,
      metricOnListBalancing = ElectricityMeterBalanceType.HOURLY
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every {
      loadElectricityMeterMeasurementsUseCase.invoke(profileId, remoteId, ElectricityMeterBalanceType.HOURLY, startTimestamp)
    } returns Maybe.just(ElectricityMeasurements(forwardActiveEnergy = 12.34f, reversedActiveEnergy = 1f))
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(
        profileId,
        remoteId,
        match { it == "1.00 kWh" }
      )
    }
  }

  @Test
  fun `should sum forwarded reactive energy entries and update formatted value`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    val endTimestamp = now.toInstant().toEpochMilli()
    val entries = listOf(
      electricityMeterLogEntity(phase1Fre = 1f, phase2Fre = 2f, phase3Fre = 3f),
      electricityMeterLogEntity(phase1Fre = 0.5f, phase2Fre = 1f, phase3Fre = 2f)
    )

    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.FORWARD_REACTIVE_ENERGY,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every { dateProvider.currentTimestamp() } returns endTimestamp
    every {
      electricityMeterLogRepository.findMeasurements(remoteId, profileId, startTimestamp, endTimestamp)
    } returns Observable.just(entries)
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify { electricityMeterLogRepository.findMeasurements(remoteId, profileId, startTimestamp, endTimestamp) }
    coVerify {
      channelValueRepository.updateAggregatedValue(
        profileId,
        remoteId,
        match { it == "9.50 kvarh" }
      )
    }
  }

  @Test
  fun `should sum reversed reactive energy entries and update formatted value`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    val endTimestamp = now.toInstant().toEpochMilli()
    val entries = listOf(
      electricityMeterLogEntity(phase1Rre = 1f, phase2Rre = 2f, phase3Rre = 3f),
      electricityMeterLogEntity(phase1Rre = 0.5f, phase2Rre = 1f, phase3Rre = 2f)
    )

    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.REVERSE_REACTIVE_ENERGY,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every { dateProvider.currentTimestamp() } returns endTimestamp
    every {
      electricityMeterLogRepository.findMeasurements(remoteId, profileId, startTimestamp, endTimestamp)
    } returns Observable.just(entries)
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify { electricityMeterLogRepository.findMeasurements(remoteId, profileId, startTimestamp, endTimestamp) }
    coVerify {
      channelValueRepository.updateAggregatedValue(
        profileId,
        remoteId,
        match { it == "9.50 kvarh" }
      )
    }
  }

  @Test
  fun `should update with summarized balance`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR,
      metricOnListBalancing = ElectricityMeterBalanceType.VECTOR
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every {
      loadElectricityMeterMeasurementsUseCase.invoke(profileId, remoteId, ElectricityMeterBalanceType.ARITHMETIC, startTimestamp)
    } returns Maybe.just(ElectricityMeasurements(forwardActiveEnergy = 20f, reversedActiveEnergy = 7.5f))
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(
        profileId,
        remoteId,
        match { it == "12.50 kWh" }
      )
    }
  }

  @Test
  fun `should update with null value`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val startTimestamp = now.withMinute(0).withSecond(0).withNano(0).toEpochSecond() * 1000
    every { userStateHolder.getElectricityMeterSettings(profileId, remoteId) } returns settings(
      metricOnList = ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE,
      metricOnListAggregation = ListValueAggregation.CURRENT_HOUR,
      metricOnListBalancing = ElectricityMeterBalanceType.DEFAULT
    )
    every { electricityMeterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { dateProvider.currentDateTime } returns now
    every {
      loadElectricityMeterMeasurementsUseCase.invoke(profileId, remoteId, ElectricityMeterBalanceType.ARITHMETIC, startTimestamp)
    } returns Maybe.error(NoSuchElementException())
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
    }
  }

  private fun settings(
    metricOnList: ElectricityMeterMeasurementType,
    metricOnListAggregation: ListValueAggregation,
    metricOnListBalancing: ElectricityMeterBalanceType = ElectricityMeterBalanceType.ARITHMETIC
  ) = ElectricityMeterSettings(
    currentMonthBalancing = ElectricityMeterBalanceType.DEFAULT,
    metricOnList = metricOnList,
    metricOnListBalancing = metricOnListBalancing,
    metricOnListAggregation = metricOnListAggregation
  )

  private fun electricityMeterLogEntity(
    phase1Fre: Float? = null,
    phase2Fre: Float? = null,
    phase3Fre: Float? = null,
    phase1Rre: Float? = null,
    phase2Rre: Float? = null,
    phase3Rre: Float? = null
  ) = ElectricityMeterLogEntity(
    id = 1L,
    channelId = remoteId,
    date = Date(0),
    phase1Fae = null,
    phase1Rae = null,
    phase1Fre = phase1Fre,
    phase1Rre = phase1Rre,
    phase2Fae = null,
    phase2Rae = null,
    phase2Fre = phase2Fre,
    phase2Rre = phase2Rre,
    phase3Fae = null,
    phase3Rae = null,
    phase3Fre = phase3Fre,
    phase3Rre = phase3Rre,
    faeBalanced = null,
    raeBalanced = null,
    manuallyComplemented = false,
    counterReset = false,
    groupingString = "",
    profileId = profileId
  )
}
