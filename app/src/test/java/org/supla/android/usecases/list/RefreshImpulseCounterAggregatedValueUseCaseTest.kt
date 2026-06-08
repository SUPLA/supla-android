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
import org.supla.android.data.model.settings.ImpulseCounterSettings
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaChannelImpulseCounterValue
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import java.time.ZonedDateTime
import java.util.Date

class RefreshImpulseCounterAggregatedValueUseCaseTest {

  @MockK
  private lateinit var channelExtendedValueRepository: ChannelExtendedValueRepository

  @MockK
  private lateinit var impulseCounterLogRepository: ImpulseCounterLogRepository

  @MockK
  private lateinit var channelValueRepository: ChannelValueRepository

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var userStateHolder: UserStateHolder

  @MockK
  private lateinit var dateProvider: DateProvider

  @InjectMockKs
  private lateinit var useCase: RefreshImpulseCounterAggregatedValueUseCase

  private val profileId = 1L
  private val remoteId = 100

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should return early when showOnList is COUNTER_STATE`() = runTest {
    // given
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.NO_AGGREGATION)
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify(exactly = 0) { dateProvider.currentDate() }
    verify { userStateHolder.getImpulseCounterSettings(profileId, remoteId) }
    confirmVerified(userStateHolder, dateProvider, impulseCounterLogRepository, channelValueRepository, channelExtendedValueRepository)
  }

  @Test
  fun `should return early when no logs are found in database`() = runTest {
    // given
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_HOUR)
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.empty()
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify {
      userStateHolder.getImpulseCounterSettings(profileId, remoteId)
      impulseCounterLogRepository.findOldestEntity(remoteId, profileId)
    }
    coVerify {
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
    }
    confirmVerified(userStateHolder, dateProvider, impulseCounterLogRepository, channelValueRepository, channelExtendedValueRepository)
  }

  @Test
  fun `should update with NO_VALUE_TEXT when filtered entries are empty`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:00:00Z")
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_HOUR)
    every { updateEventsManager.emitChannelUpdate(remoteId) } answers {}
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { dateProvider.currentDate() } returns Date(now.toInstant().toEpochMilli())
    every { dateProvider.currentDateTime } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { impulseCounterLogRepository.findMeasurements(remoteId, profileId, any(), any()) } returns Observable.just(emptyList())
    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
    }
  }

  @Test
  fun `should update with formatted aggregated value when entries are present`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_HOUR)
    val entries = listOf(
      mockk<ImpulseCounterLogEntity> { every { calculatedValue } returns 10f },
      mockk<ImpulseCounterLogEntity> { every { calculatedValue } returns 20.5f }
    )

    every { updateEventsManager.emitChannelUpdate(remoteId) } answers {}
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { dateProvider.currentDate() } returns Date(now.toInstant().toEpochMilli())
    every { dateProvider.currentDateTime } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every { impulseCounterLogRepository.findMeasurements(remoteId, profileId, any(), any()) } returns Observable.just(entries)

    val extendedValueEntity = mockk<ChannelExtendedValueEntity>()
    val impulseCounterValue = mockk<SuplaChannelImpulseCounterValue>()

    val suplaValue = SuplaChannelExtendedValue()
    suplaValue.ImpulseCounterValue = impulseCounterValue

    coEvery { channelExtendedValueRepository.findBy(profileId, remoteId) } returns extendedValueEntity
    every { extendedValueEntity.getSuplaValue() } returns suplaValue
    every { impulseCounterValue.unit } returns "m3"

    coEvery { channelValueRepository.updateAggregatedValue(profileId, remoteId, any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    coVerify {
      channelValueRepository.updateAggregatedValue(profileId, remoteId, match { it.contains("30.5") && it.contains("m3") })
    }
  }

  @Test
  fun `should use correct start date for CURRENT_DAY`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-10T10:30:00Z")
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_DAY)
    val expectedStartDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0)

    every { updateEventsManager.emitChannelUpdate(remoteId) } answers {}
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { dateProvider.currentDate() } returns Date(now.toInstant().toEpochMilli())
    every { dateProvider.currentDateTime } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    } returns
      Observable.just(emptyList())
    coEvery { channelValueRepository.updateAggregatedValue(any(), any(), any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    }
  }

  @Test
  fun `should use correct start date for CURRENT_WEEK`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:00Z") // Thursday
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_WEEK)
    // 2023-10-12 is Thursday, so Monday same week is 2023-10-09
    val expectedStartDate = ZonedDateTime.parse("2023-10-09T00:00:00Z")

    every { updateEventsManager.emitChannelUpdate(remoteId) } answers {}
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { dateProvider.currentDate() } returns Date(now.toInstant().toEpochMilli())
    every { dateProvider.currentDateTime } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    } returns
      Observable.just(emptyList())
    coEvery { channelValueRepository.updateAggregatedValue(any(), any(), any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    }
  }

  @Test
  fun `should use correct start date for CURRENT_MONTH`() = runTest {
    // given
    val now = ZonedDateTime.parse("2023-10-12T10:30:00Z")
    val settings = ImpulseCounterSettings(showOnList = ListValueAggregation.CURRENT_MONTH)
    val expectedStartDate = ZonedDateTime.parse("2023-10-01T00:00:00Z")

    every { updateEventsManager.emitChannelUpdate(remoteId) } answers {}
    every { userStateHolder.getImpulseCounterSettings(profileId, remoteId) } returns settings
    every { dateProvider.currentDate() } returns Date(now.toInstant().toEpochMilli())
    every { dateProvider.currentDateTime } returns now
    every { impulseCounterLogRepository.findOldestEntity(remoteId, profileId) } returns Maybe.just(mockk())
    every {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    } returns
      Observable.just(emptyList())
    coEvery { channelValueRepository.updateAggregatedValue(any(), any(), any()) } returns Unit

    // when
    useCase.invoke(profileId, remoteId)

    // then
    verify {
      impulseCounterLogRepository.findMeasurements(remoteId, profileId, Date(expectedStartDate.toEpochSecond() * 1000), any())
    }
  }
}
